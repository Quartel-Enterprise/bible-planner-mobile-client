package com.quare.bibleplanner.core.preferences.materialyou.domain.usecase

fun interface SetIsDynamicColorsEnabled {
    suspend operator fun invoke(isEnabled: Boolean)
}
