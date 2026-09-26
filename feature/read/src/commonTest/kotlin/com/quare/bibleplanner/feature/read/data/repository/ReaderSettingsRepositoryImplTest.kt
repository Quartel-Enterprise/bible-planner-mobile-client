package com.quare.bibleplanner.feature.read.data.repository

import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.fake.InMemoryPreferencesDataStore
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReaderSettingsRepositoryImplTest {
    private lateinit var dataStore: InMemoryPreferencesDataStore
    private lateinit var repository: ReaderSettingsRepositoryImpl

    @BeforeTest
    fun setUp() {
        dataStore = InMemoryPreferencesDataStore()
        repository = ReaderSettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `GIVEN nothing stored WHEN observing THEN falls back to the default settings`() = runTest {
        // When
        val settings = repository.observe().first()

        // Then
        assertEquals(
            expected = ReaderSettingsModel(
                fontSizeSp = ReaderFontSize.DEFAULT,
                font = ReaderFont.LORA,
                isRulerEnabled = false,
                rulerLines = ReaderRulerLines.DEFAULT,
                isFocusedVerseEnabled = false,
                isVerticalReadingEnabled = false,
            ),
            actual = settings,
        )
    }

    @Test
    fun `GIVEN every setting changed WHEN observing THEN reads each one back`() = runTest {
        // Given
        repository.setFontSize(21.5f)
        repository.setFont(ReaderFont.ATKINSON.name)
        repository.setRulerEnabled(true)
        repository.setRulerLines(3)
        repository.setFocusedVerseEnabled(true)
        repository.setVerticalReadingEnabled(true)

        // When
        val settings = repository.observe().first()

        // Then
        assertEquals(
            expected = ReaderSettingsModel(
                fontSizeSp = 21.5f,
                font = ReaderFont.ATKINSON,
                isRulerEnabled = true,
                rulerLines = 3,
                isFocusedVerseEnabled = true,
                isVerticalReadingEnabled = true,
            ),
            actual = settings,
        )
    }

    @Test
    fun `GIVEN a font the app no longer ships WHEN observing THEN falls back to the default font`() = runTest {
        // Given
        dataStore.write(
            key = stringPreferencesKey("reader_font"),
            value = "COMIC_SANS",
        )

        // When
        val settings = repository.observe().first()

        // Then
        assertEquals(
            expected = ReaderFont.LORA,
            actual = settings.font,
        )
    }
}
