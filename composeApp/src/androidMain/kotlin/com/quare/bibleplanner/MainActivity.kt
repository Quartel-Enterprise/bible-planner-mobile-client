package com.quare.bibleplanner

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                getSpecificColors = { isAppInDarkTheme ->
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
}
