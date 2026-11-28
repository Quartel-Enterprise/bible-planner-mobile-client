package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.quare.bibleplanner.feature.releasenotes.presentation.ReleaseNotesStateLayout
import com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel.ReleaseNotesViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReleaseNotesNativeRoot(openUrl: (String) -> Unit) {
    val viewModel: ReleaseNotesViewModel = koinViewModel()
    val screenFactory: ReleaseNotesNativeScreenFactory = koinInject()
    val screen = remember(screenFactory) { screenFactory.create(onEvent = viewModel::onEvent) }

    ReleaseNotesStateLayout(
        viewModel = viewModel,
        openUrl = openUrl,
    ) { uiState ->
        val nativeState = uiState.toNativeState()

        UIKitViewController(
            factory = { screen.viewController },
            modifier = Modifier.fillMaxSize(),
            update = { screen.render(nativeState) },
            properties = UIKitInteropProperties(
                // Why: the SwiftUI list owns every touch; cooperative mode would delay taps to let Compose scroll.
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                // Why: off by default, which hides the whole native screen from VoiceOver.
                isNativeAccessibilityEnabled = true,
            ),
        )
    }
}
