package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.runtime.Composable

// iOS applies runtime language changes through the ObserveAppLocale use case (NSUserDefaults), so no
// extra composable handling is needed here.
@Composable
actual fun ApplyAppLocaleEffect() = Unit
