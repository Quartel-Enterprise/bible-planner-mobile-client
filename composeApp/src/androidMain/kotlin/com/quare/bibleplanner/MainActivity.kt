package com.quare.bibleplanner

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.SystemBarStyle.Companion.dark
import androidx.activity.SystemBarStyle.Companion.light
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(
                getSpecificColors = { isAppInDarkTheme ->
                    enableEdgeToEdge(statusBarStyle = getStatusBarStyle(isAppInDarkTheme))
                    getAndroidSpecificColorScheme(isAppInDarkTheme)
                },
            )
        }
    }

    @Composable
    private fun getAndroidSpecificColorScheme(isAppInDarkTheme: Boolean): ColorScheme? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isAppInDarkTheme) {
                dynamicDarkColorScheme(this)
            } else {
                dynamicLightColorScheme(this)
            }
        } else {
            null
        }

    @Composable
    private fun getStatusBarStyle(isAppInDarkTheme: Boolean): SystemBarStyle = SystemBarStyle.run {
        val color = Color.Transparent.toArgb()
        if (isAppInDarkTheme) {
            dark(color)
        } else {
            light(color, color)
        }
    }
}
