package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderFontUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderNoteIconUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderVerticalReadingUseCase
import com.quare.bibleplanner.feature.read.fake.FakeReaderSettingsRepository
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SetReaderTypographyUseCasesTest {
    private lateinit var repository: FakeReaderSettingsRepository

    @BeforeTest
    fun setUp() {
        repository = FakeReaderSettingsRepository()
    }

    @Test
    fun `GIVEN a picked font WHEN setting the reader font THEN stores the picked font`() = runTest {
        // When
        SetReaderFontUseCase(readerSettingsRepository = repository)(ReaderFont.BITTER)

        // Then
        assertEquals(
            expected = ReaderFont.BITTER,
            actual = repository.settings.value.font,
        )
    }

    @Test
    fun `GIVEN vertical reading turned on WHEN setting it THEN stores vertical reading turned on`() = runTest {
        // When
        SetReaderVerticalReadingUseCase(readerSettingsRepository = repository)(true)

        // Then
        assertTrue(repository.settings.value.isVerticalReadingEnabled)
    }

    @Test
    fun `GIVEN the note icon turned off WHEN setting it THEN stores the note icon turned off`() = runTest {
        // When
        SetReaderNoteIconUseCase(readerSettingsRepository = repository)(false)

        // Then
        assertFalse(repository.settings.value.isNoteIconEnabled)
    }
}
