package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.runtime.Composable

// Why: JVM applies language changes through ObserveAppLocale (Locale.setDefault), so
// no composable handling is needed.
@Composable
actual fun ApplyAppLocaleEffect() = Unit
