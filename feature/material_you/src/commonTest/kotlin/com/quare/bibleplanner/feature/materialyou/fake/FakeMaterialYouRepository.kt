package com.quare.bibleplanner.feature.materialyou.fake

import com.quare.bibleplanner.feature.materialyou.domain.repository.MaterialYouRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeMaterialYouRepository(
    isDynamicColorsEnabled: Boolean,
) : MaterialYouRepository {
    val isDynamicColorsEnabled = MutableStateFlow(isDynamicColorsEnabled)
    val writes = mutableListOf<Boolean>()

    override fun getIsDynamicColorsEnabledFlow(): Flow<Boolean> = isDynamicColorsEnabled

    override suspend fun setIsDynamicColorsEnabled(isEnabled: Boolean) {
        writes += isEnabled
        isDynamicColorsEnabled.value = isEnabled
    }
}
