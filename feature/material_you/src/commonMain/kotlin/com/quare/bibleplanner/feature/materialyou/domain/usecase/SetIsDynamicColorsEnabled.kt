package com.quare.bibleplanner.feature.materialyou.domain.usecase

fun interface SetIsDynamicColorsEnabled {
    suspend operator fun invoke(isEnabled: Boolean)
}
