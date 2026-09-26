package com.quare.bibleplanner.feature.daystudy.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DayStudyPanelRatioRepositoryImplTest {
    private lateinit var repository: DayStudyPanelRatioRepositoryImpl

    @BeforeTest
    fun setUp() {
        repository = DayStudyPanelRatioRepositoryImpl(InMemoryPreferencesDataStore())
    }

    @Test
    fun `GIVEN nothing stored WHEN observing the reading fraction THEN emits null`() = runTest {
        // When
        val fraction = repository.observeReadingFraction().first()

        // Then
        assertNull(fraction)
    }

    @Test
    fun `GIVEN a stored fraction WHEN observing it THEN emits the stored value`() = runTest {
        // Given
        repository.setReadingFraction(0.55f)

        // When
        val fraction = repository.observeReadingFraction().first()

        // Then
        assertEquals(0.55f, fraction)
    }
}

private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val preferences = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        preferences.update { current -> transform(current) }
        return preferences.value
    }
}
