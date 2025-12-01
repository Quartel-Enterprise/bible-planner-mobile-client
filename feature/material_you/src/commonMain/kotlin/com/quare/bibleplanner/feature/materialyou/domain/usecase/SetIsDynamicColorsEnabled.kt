package com.quare.bibleplanner.feature.materialyou.domain.usecase

interface SetIsDynamicColorsEnabled {
    suspend operator fun invoke(isEnabled: Boolean)
}
