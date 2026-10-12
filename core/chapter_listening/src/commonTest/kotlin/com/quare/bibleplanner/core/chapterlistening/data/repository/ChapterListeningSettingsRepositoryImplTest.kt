package com.quare.bibleplanner.core.chapterlistening.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.provider.datastore.testing.FakePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChapterListeningSettingsRepositoryImplTest {
    private lateinit var repository: ChapterListeningSettingsRepositoryImpl

    @BeforeTest
    fun setUp() {
        repository = ChapterListeningSettingsRepositoryImpl(FakePreferencesDataStore(emptyPreferences()))
    }

    @Test
    fun `GIVEN nothing saved WHEN observing THEN reads at normal speed with auto next and word highlight`() = runTest {
        // When
        val settings = repository.observe().first()

        // Then
        assertEquals(
            ChapterListeningSettingsModel(
                speed = 1f,
                isAutoNextEnabled = true,
                isWordHighlightEnabled = true,
            ),
            settings,
        )
    }

    @Test
    fun `GIVEN saved choices WHEN observing THEN reads them back`() = runTest {
        // When
        repository.setSpeed(1.5f)
        repository.setAutoNextEnabled(false)
        repository.setWordHighlightEnabled(false)

        // Then
        assertEquals(
            ChapterListeningSettingsModel(
                speed = 1.5f,
                isAutoNextEnabled = false,
                isWordHighlightEnabled = false,
            ),
            repository.observe().first(),
        )
    }
}
