package com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.materialyou.fake.FakeMaterialYouRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GetIsDynamicColorsEnabledFlowUseCaseTest {
    private lateinit var useCase: GetIsDynamicColorsEnabledFlowUseCase

    @Test
    fun `GIVEN dynamic colors enabled WHEN observing them THEN emits enabled`() = runTest {
        // When
        val isEnabled = useCase().first()

        // Then
        assertTrue(isEnabled)
    }

    @BeforeTest
    fun setUp() {
        useCase = GetIsDynamicColorsEnabledFlowUseCase(FakeMaterialYouRepository(isDynamicColorsEnabled = true))
    }
}
