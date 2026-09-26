package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderFontUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderVerticalReadingUseCase
import com.quare.bibleplanner.feature.read.fake.FakeReaderSettingsRepository
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SetReaderTypographyUseCasesTest {
    private lateinit var repository: FakeReaderSettingsRepository

    @BeforeTest
    fun setUp() {
        repository = FakeReaderSettingsRepository()
    }

    @Test
    fun `stores the picked font`() = runTest {
        // When
        SetReaderFontUseCase(readerSettingsRepository = repository)(ReaderFont.BITTER)

        // Then
        assertEquals(
            expected = ReaderFont.BITTER,
            actual = repository.settings.value.font,
        )
    }

    @Test
    fun `stores vertical reading turned on`() = runTest {
        // When
        SetReaderVerticalReadingUseCase(readerSettingsRepository = repository)(true)

        // Then
        assertTrue(repository.settings.value.isVerticalReadingEnabled)
    }
}
