package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * A reusable composable utility for collecting emissions from a [Flow] inside Compose.
 *
 * This function launches a coroutine tied to the composition lifecycle and collects
 * the latest values emitted by the given [flow]. For each new emission, the provided
 * [emit] lambda is invoked, allowing the caller to perform side effects such as
 * showing a snackbar, navigating to another screen, or triggering UI actions.
 *
 * The collection runs within a [LaunchedEffect] keyed by [Unit], meaning it will
 * start once when this composable enters the composition and will automatically
 * cancel when it leaves.
 *
 * Example usage:
 * ```
 * ActionCollector(viewModel.uiActions) { uiAction ->
 *     when (uiAction) {
 *         is ShowSnackbar -> scaffoldState.snackbarHostState.showSnackbar(uiAction.message)
 *         is NavigateTo -> navController.navigate(uiAction.route)
 *     }
 * }
 * ```
 *
 * @param flow The [Flow] of values to collect. Typically a [Flow] of UI events
 *             or one-off actions exposed by a ViewModel.
 * @param emit A suspend lambda executed for each new value emitted by [flow].
 *             Use this to perform side effects in response to events.
 */
@Composable
fun <T> ActionCollector(
    flow: Flow<T>,
    emit: suspend (T) -> Unit,
) {
    LaunchedEffect(key1 = Unit) {
        flow.collectLatest {
            emit(it)
        }
    }
}
