package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.runtime.Composable

// JVM applies runtime language changes through the ObserveAppLocale use case (Locale.setDefault), so no
// extra composable handling is needed here.
@Composable
actual fun ApplyAppLocaleEffect() = Unit
