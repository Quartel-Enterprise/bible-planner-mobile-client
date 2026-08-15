package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderFocusAidUseCase
import com.quare.bibleplanner.feature.read.fake.FakeReaderSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SetReaderFocusAidUseCaseTest {
    private lateinit var useCase: SetReaderFocusAidUseCase
    private lateinit var repository: FakeReaderSettingsRepository

    @BeforeTest
    fun setUp() {
        repository = FakeReaderSettingsRepository()
        useCase = SetReaderFocusAidUseCase(readerSettingsRepository = repository)
    }

    @Test
    fun `turning the ruler on turns the focused verse off`() = runTest {
        // Given
        useCase(ReaderFocusAid.FOCUSED_VERSE)

        // When
        useCase(ReaderFocusAid.RULER)

        // Then
        assertTrue(repository.settings.value.isRulerEnabled)
        assertFalse(repository.settings.value.isFocusedVerseEnabled)
    }

    @Test
    fun `turning the focused verse on turns the ruler off`() = runTest {
        // Given
        useCase(ReaderFocusAid.RULER)

        // When
        useCase(ReaderFocusAid.FOCUSED_VERSE)

        // Then
        assertTrue(repository.settings.value.isFocusedVerseEnabled)
        assertFalse(repository.settings.value.isRulerEnabled)
    }

    @Test
    fun `choosing no aid turns both off`() = runTest {
        // Given
        useCase(ReaderFocusAid.RULER)

        // When
        useCase(ReaderFocusAid.NONE)

        // Then
        assertFalse(repository.settings.value.isRulerEnabled)
        assertFalse(repository.settings.value.isFocusedVerseEnabled)
    }
}
