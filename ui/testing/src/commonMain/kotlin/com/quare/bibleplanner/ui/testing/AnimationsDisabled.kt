package com.quare.bibleplanner.ui.testing

import androidx.compose.ui.MotionDurationScale

// The effect context of runComposeUiTest that plays every animation in 0 ms, as the device's
// "Remove animations" setting does.
object AnimationsDisabled : MotionDurationScale {
    override val scaleFactor: Float = 0f
}
