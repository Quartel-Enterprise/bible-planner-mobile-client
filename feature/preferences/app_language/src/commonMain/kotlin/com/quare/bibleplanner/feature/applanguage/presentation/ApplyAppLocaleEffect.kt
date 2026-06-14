package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.runtime.Composable

/**
 * Keeps the platform locale in step with the persisted app language for changes that happen while the
 * app is running — most importantly language updates arriving from cross-device sync.
 *
 * On Android this swaps the stored locale and recreates the activity (the only way to re-apply Android
 * resources at runtime). On iOS/JVM the `ObserveAppLocale` use case already applies runtime changes, so
 * the effect is a no-op there. Mount it once near the app root.
 */
@Composable
expect fun ApplyAppLocaleEffect()
