package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.PendingBibleUpdatesNavRoute
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PendingBibleUpdatesPromptOverlay() {
    val viewModel = koinViewModel<PendingBibleUpdatesPromptViewModel>()
    val navigator = koinInject<Navigator>()
    val shouldPrompt by viewModel.shouldPrompt.collectAsState()
    LaunchedEffect(shouldPrompt) {
        if (shouldPrompt) {
            viewModel.onPromptConsumed()
            navigator.navigate(PendingBibleUpdatesNavRoute)
        }
    }
}
