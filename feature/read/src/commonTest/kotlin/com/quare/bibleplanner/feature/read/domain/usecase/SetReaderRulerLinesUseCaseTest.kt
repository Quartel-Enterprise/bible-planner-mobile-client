package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderRulerLinesUseCase
import com.quare.bibleplanner.feature.read.fake.FakeReaderSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SetReaderRulerLinesUseCaseTest {
    private lateinit var useCase: SetReaderRulerLinesUseCase
    private lateinit var repository: FakeReaderSettingsRepository

    @BeforeTest
    fun setUp() {
        repository = FakeReaderSettingsRepository()
        useCase = SetReaderRulerLinesUseCase(readerSettingsRepository = repository)
    }

    @Test
    fun `stores a line count inside the bounds as it is`() = runTest {
        // When
        useCase(3)

        // Then
        assertEquals(
            expected = 3,
            actual = repository.settings.value.rulerLines,
        )
    }

    @Test
    fun `clamps a line count below the minimum`() = runTest {
        // When
        useCase(0)

        // Then
        assertEquals(
            expected = ReaderRulerLines.MIN,
            actual = repository.settings.value.rulerLines,
        )
    }

    @Test
    fun `clamps a line count above the maximum`() = runTest {
        // When
        useCase(10)

        // Then
        assertEquals(
            expected = ReaderRulerLines.MAX,
            actual = repository.settings.value.rulerLines,
        )
    }
}
