package com.quare.bibleplanner.core.preferences.themeselection.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.data.mapper.ThemePreferenceMapperImpl
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ThemeSelectionRepositoryImplTest {
    private lateinit var repository: ThemeSelectionRepositoryImpl
    private lateinit var syncedPreferenceDao: FakeSyncedPreferenceDao

    @Test
    fun `GIVEN nothing stored WHEN reading the theme and contrast THEN returns the defaults`() = runTest {
        // Given
        prepareScenario()

        // When
        val theme = repository.getThemeFlow().first()

        // Then
        assertEquals(Theme.SYSTEM, theme)
        assertEquals(ContrastType.Standard, repository.getContrastTypeFlow().first())
    }

    @Test
    fun `GIVEN sync disabled WHEN setting the theme THEN stores it only on the device`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setTheme(Theme.DARK)

        // Then
        assertEquals(Theme.DARK, repository.getThemeFlow().first())
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    @Test
    fun `GIVEN sync enabled WHEN setting the theme THEN also mirrors it for the other devices`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.THEME_SYNC_ENABLED to "true"))

        // When
        repository.setTheme(Theme.LIGHT)

        // Then
        assertEquals(
            listOf(Triple(SyncedPreferenceKeys.APP_THEME, "light_theme", TIMESTAMP)),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN sync enabled WHEN setting the contrast THEN stores and mirrors it`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.THEME_SYNC_ENABLED to "true"))

        // When
        repository.setContrastType(ContrastType.Medium)

        // Then
        assertEquals(ContrastType.Medium, repository.getContrastTypeFlow().first())
        assertEquals(
            listOf(Triple(SyncedPreferenceKeys.THEME_CONTRAST, "medium_contrast", TIMESTAMP)),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN sync disabled WHEN setting the contrast THEN does not mirror it`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setContrastType(ContrastType.High)

        // Then
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    @Test
    fun `GIVEN a device theme WHEN enabling sync THEN publishes the flag with the current theme and contrast`() =
        runTest {
            // Given
            prepareScenario()
            repository.setTheme(Theme.DARK)
            repository.setContrastType(ContrastType.High)

            // When
            repository.setThemeSyncEnabled(true)

            // Then
            assertTrue(repository.getThemeSyncEnabledFlow().first())
            assertEquals(
                listOf(
                    Triple(SyncedPreferenceKeys.THEME_SYNC_ENABLED, "true", TIMESTAMP),
                    Triple(SyncedPreferenceKeys.APP_THEME, "dark_theme", TIMESTAMP),
                    Triple(SyncedPreferenceKeys.THEME_CONTRAST, "high_contrast", TIMESTAMP),
                ),
                syncedPreferenceDao.localWrites,
            )
        }

    @Test
    fun `GIVEN sync enabled WHEN disabling sync THEN only publishes the flag`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.THEME_SYNC_ENABLED to "true"))

        // When
        repository.setThemeSyncEnabled(false)

        // Then
        assertFalse(repository.getThemeSyncEnabledFlow().first())
        assertEquals(
            listOf(Triple(SyncedPreferenceKeys.THEME_SYNC_ENABLED, "false", TIMESTAMP)),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN values synced from another device WHEN observing them THEN maps them to the theme and contrast`() =
        runTest {
            // Given
            prepareScenario(
                syncedValues = mapOf(
                    SyncedPreferenceKeys.APP_THEME to "dark_theme",
                    SyncedPreferenceKeys.THEME_CONTRAST to "high_contrast",
                ),
            )

            // When
            val syncedTheme = repository.observeSyncedTheme().first()

            // Then
            assertEquals(Theme.DARK, syncedTheme)
            assertEquals(ContrastType.High, repository.observeSyncedContrast().first())
        }

    @Test
    fun `GIVEN nothing synced yet WHEN observing the synced values THEN returns no values`() = runTest {
        // Given
        prepareScenario()

        // When
        val syncedTheme = repository.observeSyncedTheme().first()

        // Then
        assertNull(syncedTheme)
        assertNull(repository.observeSyncedContrast().first())
    }

    @Test
    fun `GIVEN sync enabled WHEN applying synced values THEN stores them without echoing them back`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.THEME_SYNC_ENABLED to "true"))

        // When
        repository.applySyncedTheme(Theme.LIGHT)
        repository.applySyncedContrast(ContrastType.Medium)

        // Then
        assertEquals(Theme.LIGHT, repository.getThemeFlow().first())
        assertEquals(ContrastType.Medium, repository.getContrastTypeFlow().first())
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    private fun prepareScenario(syncedValues: Map<String, String> = emptyMap()) {
        syncedPreferenceDao = FakeSyncedPreferenceDao(syncedValues)
        repository = ThemeSelectionRepositoryImpl(
            dataStore = FakePreferencesDataStore(),
            mapper = ThemePreferenceMapperImpl(),
            syncedPreferenceDao = syncedPreferenceDao,
            currentTimestampProvider = { TIMESTAMP },
        )
    }

    private companion object {
        const val TIMESTAMP = 1_000L
    }
}

private class FakePreferencesDataStore : DataStore<Preferences> {
    private val preferences = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
        transform(preferences.value).also { updated -> preferences.value = updated }
}

private class FakeSyncedPreferenceDao(
    initialValues: Map<String, String>,
) : SyncedPreferenceDao() {
    private val values = MutableStateFlow(initialValues)
    val localWrites = mutableListOf<Triple<String, String, Long>>()

    override fun observeValue(key: String): Flow<String?> = values.map { it[key] }

    override suspend fun setLocal(
        key: String,
        value: String,
        updatedAt: Long,
    ) {
        localWrites += Triple(key, value, updatedAt)
        values.update { it + (key to value) }
    }

    override fun getPendingFlow(): Flow<List<SyncedPreferenceEntity>> = error("unused")

    override suspend fun getPending(): List<SyncedPreferenceEntity> = error("unused")

    override suspend fun markSynced(
        key: String,
        syncedUpdatedAt: Long,
    ) = error("unused")

    override suspend fun seedProvisional(
        key: String,
        value: String,
    ) = error("unused")

    override suspend fun adoptProvisional(now: Long) = error("unused")

    override suspend fun deleteByKeys(keys: List<String>) = error("unused")

    override suspend fun deleteAll() = error("unused")

    override suspend fun updateFromRemote(
        key: String,
        value: String,
        remoteUpdatedAt: Long,
    ): Int = error("unused")

    override suspend fun insertIfAbsent(entity: SyncedPreferenceEntity) = error("unused")
}
