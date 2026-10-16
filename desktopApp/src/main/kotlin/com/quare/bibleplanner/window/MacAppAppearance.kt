package com.quare.bibleplanner.window

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.utils.DesktopOs
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

/*
 * Why: AWT pins NSApp to the light Aqua appearance at launch and only reads
 * apple.awt.application.appearance once, so the macOS title bar stayed white under the dark
 * theme. Setting NSApp.appearance through the Objective-C runtime, on the AppKit main thread,
 * follows the theme picked in the app without a restart.
 */
internal object MacAppAppearance {
    private val isMac = DesktopOs.detect() == DesktopOs.MAC
    private val objc by lazy { NativeLibrary.getInstance("objc") }
    private val msgSend by lazy { objc.getFunction("objc_msgSend") }
    private val objcGetClass by lazy { objc.getFunction("objc_getClass") }
    private val selRegisterName by lazy { objc.getFunction("sel_registerName") }
    private val autoreleasePoolPush by lazy { objc.getFunction("objc_autoreleasePoolPush") }
    private val autoreleasePoolPop by lazy { objc.getFunction("objc_autoreleasePoolPop") }
    private var appliedIsDarkTheme: Boolean? = null

    /*
     * Why: AWT reads this property once when it starts, so it must be set before application {}.
     * The first frame then matches the OS appearance instead of flashing Aqua before apply() runs.
     */
    fun followSystemAtLaunch() {
        if (isMac) System.setProperty(APPEARANCE_PROPERTY, APPEARANCE_SYSTEM)
    }

    fun apply(isDarkTheme: Boolean) {
        if (appliedIsDarkTheme == isDarkTheme) return
        // Why: set before the native call so a broken JNA setup isn't retried on every recomposition.
        appliedIsDarkTheme = isDarkTheme
        runCatching { setApplicationAppearance(if (isDarkTheme) DARK_AQUA else AQUA) }
            .onFailure { throwable -> Logger.e(throwable) { "Failed to set the macOS app appearance" } }
    }

    private fun setApplicationAppearance(appearanceName: String) {
        val pool = autoreleasePoolPush.invokePointer(emptyArray())
        try {
            val appearance = send(
                receiver = getClass("NSAppearance"),
                selector = "appearanceNamed:",
                args = listOf(
                    send(
                        receiver = getClass("NSString"),
                        selector = "stringWithUTF8String:",
                        args = listOf(appearanceName),
                    ),
                ),
            )
            send(
                receiver = send(
                    receiver = getClass("NSApplication"),
                    selector = "sharedApplication",
                    args = emptyList(),
                ),
                selector = "performSelectorOnMainThread:withObject:waitUntilDone:",
                args = listOf(
                    registerSelector("setAppearance:"),
                    appearance,
                    false,
                ),
            )
        } finally {
            autoreleasePoolPop.invokeVoid(arrayOf(pool))
        }
    }

    private fun send(
        receiver: Pointer,
        selector: String,
        args: List<Any>,
    ): Pointer = msgSend.invokePointer((listOf(receiver, registerSelector(selector)) + args).toTypedArray())

    private fun getClass(name: String): Pointer = objcGetClass.invokePointer(arrayOf(name))

    private fun registerSelector(name: String): Pointer = selRegisterName.invokePointer(arrayOf(name))

    private const val APPEARANCE_PROPERTY = "apple.awt.application.appearance"
    private const val APPEARANCE_SYSTEM = "system"
    private const val AQUA = "NSAppearanceNameAqua"
    private const val DARK_AQUA = "NSAppearanceNameDarkAqua"
}
