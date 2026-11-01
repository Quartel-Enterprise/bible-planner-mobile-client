package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen

import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model.ReleaseNotesNativeState
import platform.UIKit.UIViewController

interface ReleaseNotesNativeScreen {
    val viewController: UIViewController

    fun render(state: ReleaseNotesNativeState)
}
