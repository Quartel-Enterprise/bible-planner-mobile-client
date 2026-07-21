package com.quare.bibleplanner.ui.component

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogWindowProvider

@Composable
actual fun DialogWindowBlurEffect(radius: Dp) {
    val view = LocalView.current
    val radiusPx = with(LocalDensity.current) { radius.roundToPx() }
    SideEffect {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@SideEffect
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.attributes = window.attributes.apply { blurBehindRadius = radiusPx }
    }
}
