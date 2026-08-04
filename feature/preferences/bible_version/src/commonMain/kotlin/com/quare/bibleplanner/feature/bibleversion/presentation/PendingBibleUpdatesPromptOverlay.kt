package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.PendingBibleUpdatesNavRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PendingBibleUpdatesPromptOverlay(onNavigate: (NavKey) -> Unit) {
    val viewModel = koinViewModel<PendingBibleUpdatesPromptViewModel>()
    val shouldPrompt by viewModel.shouldPrompt.collectAsState()
    LaunchedEffect(shouldPrompt) {
        if (shouldPrompt) {
            viewModel.onPromptConsumed()
            onNavigate(PendingBibleUpdatesNavRoute)
        }
    }
}
