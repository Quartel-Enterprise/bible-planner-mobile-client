package com.quare.bibleplanner.window

import com.quare.bibleplanner.core.utils.DesktopOs
import java.awt.Window

internal class DesktopWindowAppearance(
    window: Window,
) {
    private val applyTheme: (isDarkTheme: Boolean) -> Unit = when (DesktopOs.detect()) {
        DesktopOs.MAC -> MacAppAppearance::apply

        DesktopOs.WINDOWS -> WindowsTitleBarAppearance(window)::apply

        // Why: GNOME/KDE draw the decorations from the desktop theme; there's no per-window API.
        DesktopOs.LINUX -> { _ -> }
    }

    fun apply(isDarkTheme: Boolean) {
        applyTheme(isDarkTheme)
    }
}
