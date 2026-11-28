package com.quare.bibleplanner.android.designsystem.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

private const val NO_DIM = 0f

/*
 * Why: a native dialog opens its own window inside the window of the navigation dialog scene, and both
 * would dim the screen. Clearing the scene window's dim leaves the single scrim of a regular dialog.
 */
@Composable
fun HideSceneDialogScrimEffect() {
    val sceneWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    DisposableEffect(sceneWindow) {
        sceneWindow?.setDimAmount(NO_DIM)
        onDispose {}
    }
}
