package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.runtime.Composable

/*
 * Why: Android can only re-apply resources by recreating the activity, so runtime language changes
 * (e.g. from sync) need this; iOS/JVM already apply them via ObserveAppLocale, so it's a no-op.
 */
@Composable
expect fun ApplyAppLocaleEffect()
