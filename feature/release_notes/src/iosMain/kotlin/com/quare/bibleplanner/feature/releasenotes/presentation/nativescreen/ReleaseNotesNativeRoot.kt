package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiAction
import com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel.ReleaseNotesViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ReleaseNotesNativeRoot(
    openUrl: (String) -> Unit,
    viewModel: ReleaseNotesViewModel = koinViewModel(),
    screenFactory: ReleaseNotesNativeScreenFactory = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is ReleaseNotesUiAction.OpenUrl -> openUrl(action.url)
        }
    }

    val screen = remember(screenFactory) { screenFactory.create(onEvent = viewModel::onEvent) }
    val nativeState = uiState.toNativeState()

    UIKitViewController(
        factory = { screen.viewController },
        modifier = Modifier.fillMaxSize(),
        update = { screen.render(nativeState) },
        // Why: the SwiftUI list owns every touch; cooperative mode would delay taps to let Compose scroll.
        properties = UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative),
    )
}
