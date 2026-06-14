package com.quare.bibleplanner.feature.themeselection.domain.usecase

fun interface ObserveThemeSync {
    suspend operator fun invoke()
}
