package com.quare.bibleplanner.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/*
 * Why: the screen lives in the entry's ViewModelStore, like the ViewModel its Compose version would get, so switching
 * tabs keeps the SwiftUI screen and its ViewModel instead of building both again.
 */
@Composable
internal fun NativeScreenContent(
    key: String,
    createScreen: () -> NativeScreen,
    modifier: Modifier = Modifier,
) {
    val owner = viewModel(key = key) { NativeScreenOwner(createScreen()) }
    UIKitViewController(
        factory = { owner.screen.viewController },
        modifier = modifier,
        properties = UIKitInteropProperties(
            // Why: the SwiftUI list owns every touch; cooperative mode would delay taps to let Compose scroll.
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            // Why: off by default, which hides the whole native screen from VoiceOver.
            isNativeAccessibilityEnabled = true,
        ),
    )
}

private class NativeScreenOwner(
    val screen: NativeScreen,
) : ViewModel() {
    override fun onCleared() {
        screen.dispose()
    }
}
