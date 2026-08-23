package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderFontSizeUseCase
import com.quare.bibleplanner.feature.read.fake.FakeReaderSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SetReaderFontSizeUseCaseTest {
    private lateinit var useCase: SetReaderFontSizeUseCase
    private lateinit var repository: FakeReaderSettingsRepository

    @BeforeTest
    fun setUp() {
        repository = FakeReaderSettingsRepository()
        useCase = SetReaderFontSizeUseCase(readerSettingsRepository = repository)
    }

    @Test
    fun `stores a size inside the slider bounds as it is`() = runTest {
        // When
        useCase(20f)

        // Then
        assertEquals(
            expected = 20f,
            actual = repository.settings.value.fontSizeSp,
        )
    }

    @Test
    fun `clamps a size below the minimum`() = runTest {
        // When
        useCase(8f)

        // Then
        assertEquals(
            expected = ReaderFontSize.MIN,
            actual = repository.settings.value.fontSizeSp,
        )
    }

    @Test
    fun `clamps a size above the maximum`() = runTest {
        // When
        useCase(64f)

        // Then
        assertEquals(
            expected = ReaderFontSize.MAX,
            actual = repository.settings.value.fontSizeSp,
        )
    }
}
