package com.quare.bibleplanner.android.designsystem.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable

/*
 * Why: the app root still applies the shared theme (colors, contrast, Material You), so a native screen
 * keeps those colors and only adds Material 3 Expressive's motion and shapes on top.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = MaterialTheme.colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = MaterialTheme.shapes,
        typography = MaterialTheme.typography,
        content = content,
    )
}
