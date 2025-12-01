package com.quare.bibleplanner

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val supportsDynamicColors: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
internal fun getAndroidSpecificColorScheme(
    isDynamicColorsOn: Boolean,
    isAppInDarkTheme: Boolean,
): ColorScheme? = if (supportsDynamicColors && isDynamicColorsOn) {
    val context = LocalContext.current
    if (isAppInDarkTheme) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
} else {
    null
}
