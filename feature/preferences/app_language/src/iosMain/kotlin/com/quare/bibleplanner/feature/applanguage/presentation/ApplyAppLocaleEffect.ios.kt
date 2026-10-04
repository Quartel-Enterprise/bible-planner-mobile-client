package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.runtime.Composable

// Why: iOS applies runtime language changes via ObserveAppLocale (NSUserDefaults), so no
// composable handling is needed.
@Composable
actual fun ApplyAppLocaleEffect() {}
