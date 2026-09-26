package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase

fun interface ObserveThemeSync {
    suspend operator fun invoke()
}
