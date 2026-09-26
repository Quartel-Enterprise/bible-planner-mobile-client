package com.quare.bibleplanner.e2e.harness

// The Android host has no Instrumentation to start the app's activity with, and build-logic keeps
// every *UiTest off it, so the flows never ask for this. It only lets commonTest compile for the host.
internal actual val e2ePlatform: E2ePlatform
    get() = error("The end-to-end flows run on the JVM and on an Android device, not on the Android host")
