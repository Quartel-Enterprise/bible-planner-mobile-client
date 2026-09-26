package com.quare.bibleplanner.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PreferencesExtensionsTest {
    private val counterKey = intPreferencesKey("counter")
    private lateinit var dataStore: DataStore<Preferences>

    @Test
    fun `GIVEN an unset key WHEN reading it THEN returns null`() = runTest {
        // When
        val value = dataStore.read(counterKey)

        // Then
        assertNull(value)
    }

    @Test
    fun `GIVEN a written key WHEN reading it THEN returns the last written value`() = runTest {
        // Given
        dataStore.write(
            key = counterKey,
            value = 1,
        )
        dataStore.write(
            key = counterKey,
            value = 2,
        )

        // When
        val value = dataStore.read(counterKey)

        // Then
        assertEquals(2, value)
    }

    @BeforeTest
    fun setUp() {
        dataStore = InMemoryPreferencesDataStore()
    }
}

private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
        transform(state.value).also { updated -> state.value = updated }
}
