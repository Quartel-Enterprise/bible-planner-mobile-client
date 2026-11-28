package com.quare.bibleplanner.android.designsystem.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

private const val NO_DIM = 0f

/*
 * Why: a native dialog opens its own window inside the window of the navigation dialog scene, and both
 * would dim the screen. This clears the dim of whatever dialog window hosts the caller, which leaves the
 * single scrim of a regular dialog. The shared ClearDialogWindowDimEffect in ui:component does the same
 * for the legacy UI.
 */
@Composable
fun HideSceneDialogScrimEffect() {
    val sceneWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    DisposableEffect(sceneWindow) {
        sceneWindow?.setDimAmount(NO_DIM)
        onDispose {}
    }
}
