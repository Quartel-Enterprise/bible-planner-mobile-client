package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.usecase.impl.ObserveReaderSettingsUseCase
import com.quare.bibleplanner.feature.read.fake.FakeReaderSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveReaderSettingsUseCaseTest {
    private lateinit var useCase: ObserveReaderSettingsUseCase
    private lateinit var repository: FakeReaderSettingsRepository

    @BeforeTest
    fun setUp() {
        repository = FakeReaderSettingsRepository()
        useCase = ObserveReaderSettingsUseCase(readerSettingsRepository = repository)
    }

    @Test
    fun `clamps a stored size that falls outside the current bounds`() = runTest {
        // Given
        repository.settings.value = repository.settings.value.copy(fontSizeSp = 48f)

        // When
        val settings = useCase().first()

        // Then
        assertEquals(
            expected = ReaderFontSize.MAX,
            actual = settings.fontSizeSp,
        )
    }
}
