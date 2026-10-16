package com.quare.bibleplanner.window

import co.touchlab.kermit.Logger
import com.sun.jna.Function
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import java.awt.Window
import java.awt.event.HierarchyEvent

/*
 * Why: Windows draws a light title bar unless DWM is told the window is dark, and AWT never tells
 * it, so the title bar stayed white under the dark theme. DWMWA_USE_IMMERSIVE_DARK_MODE switches
 * it per window, live, on Windows 10 1809+ and Windows 11.
 */
internal class WindowsTitleBarAppearance(
    private val window: Window,
) {
    // Why: Win32 functions are stdcall, which only differs from the C convention on 32-bit Windows.
    private val dwmSetWindowAttribute by lazy {
        NativeLibrary.getInstance("dwmapi").getFunction("DwmSetWindowAttribute", Function.ALT_CONVENTION)
    }
    private val setWindowPos by lazy {
        NativeLibrary.getInstance("user32").getFunction("SetWindowPos", Function.ALT_CONVENTION)
    }

    // Why: Windows 10 builds before 20H1 only understand the undocumented attribute 19.
    private val immersiveDarkModeAttributes =
        listOf(DWMWA_USE_IMMERSIVE_DARK_MODE, DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1)
    private var requestedIsDarkTheme: Boolean? = null
    private var appliedIsDarkTheme: Boolean? = null

    init {
        /*
         * Why: the first theme usually resolves before AWT creates the HWND. Applying it when the
         * window becomes displayable, before it is shown, avoids a light title bar flashing first,
         * and re-applies it if AWT ever recreates the native window.
         */
        window.addHierarchyListener { event ->
            if (event.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong() == 0L) return@addHierarchyListener
            appliedIsDarkTheme = null
            if (window.isDisplayable) requestedIsDarkTheme?.let(::applyToNativeWindow)
        }
    }

    fun apply(isDarkTheme: Boolean) {
        requestedIsDarkTheme = isDarkTheme
        if (window.isDisplayable) applyToNativeWindow(isDarkTheme)
    }

    private fun applyToNativeWindow(isDarkTheme: Boolean) {
        if (appliedIsDarkTheme == isDarkTheme) return
        // Why: set before the native call so a broken JNA setup isn't retried on every recomposition.
        appliedIsDarkTheme = isDarkTheme
        runCatching { setImmersiveDarkMode(Native.getWindowPointer(window), isDarkTheme) }
            .onFailure { throwable -> Logger.e(throwable) { "Failed to set the Windows title bar theme" } }
    }

    private fun setImmersiveDarkMode(
        hwnd: Pointer,
        isDarkTheme: Boolean,
    ) {
        val value = IntByReference(if (isDarkTheme) 1 else 0)
        val isApplied = immersiveDarkModeAttributes.any { attribute ->
            dwmSetWindowAttribute.invokeInt(arrayOf(hwnd, attribute, value, Int.SIZE_BYTES)) == S_OK
        }
        if (!isApplied) {
            // Why: Windows 8.1 and Windows 10 before 1809 have no dark title bar; that's expected, not an error.
            Logger.w { "This Windows version doesn't support a dark title bar" }
            return
        }
        // Why: DWM only repaints the non-client area on the next frame change, so force one now.
        setWindowPos.invokeInt(
            arrayOf(
                hwnd,
                Pointer.NULL,
                0,
                0,
                0,
                0,
                SWP_REPAINT_FRAME_FLAGS,
            ),
        )
    }

    private companion object {
        const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
        const val DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19
        const val S_OK = 0
        const val SWP_NOSIZE = 0x0001
        const val SWP_NOMOVE = 0x0002
        const val SWP_NOZORDER = 0x0004
        const val SWP_NOACTIVATE = 0x0010
        const val SWP_FRAMECHANGED = 0x0020

        // Why: the HWND belongs to the AWT toolkit thread; async avoids blocking the EDT on it.
        const val SWP_ASYNCWINDOWPOS = 0x4000
        const val SWP_REPAINT_FRAME_FLAGS =
            SWP_NOSIZE or SWP_NOMOVE or SWP_NOZORDER or SWP_NOACTIVATE or SWP_FRAMECHANGED or SWP_ASYNCWINDOWPOS
    }
}
