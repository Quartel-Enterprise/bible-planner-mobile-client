package com.quare.bibleplanner.nativeui

/*
 * Why: implemented in Swift, one function per screen drawn in SwiftUI, so Kotlin decides where each one shows and
 * Swift fails to compile when it misses one.
 */
fun interface NativeScreenFactory {
    fun createReadingPlanScreen(): NativeScreen
}
