package com.quare.bibleplanner.core.preferences.materialyou.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.preferences.materialyou.fake.FakePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MaterialYouRepositoryImplTest {
    private lateinit var repository: MaterialYouRepositoryImpl

    @Test
    fun `GIVEN nothing stored WHEN observing dynamic colors THEN reports them disabled`() = runTest {
        // When
        val isEnabled = repository.getIsDynamicColorsEnabledFlow().first()

        // Then
        assertFalse(isEnabled)
    }

    @Test
    fun `GIVEN dynamic colors turned on WHEN observing them THEN reports them enabled`() = runTest {
        // Given
        repository.setIsDynamicColorsEnabled(true)

        // When
        val isEnabled = repository.getIsDynamicColorsEnabledFlow().first()

        // Then
        assertTrue(isEnabled)
    }

    @BeforeTest
    fun setUp() {
        repository = MaterialYouRepositoryImpl(FakePreferencesDataStore(emptyPreferences()))
    }
}
