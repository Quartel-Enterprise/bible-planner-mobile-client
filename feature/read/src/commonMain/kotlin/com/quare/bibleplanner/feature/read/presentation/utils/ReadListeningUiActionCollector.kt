package com.quare.bibleplanner.feature.read.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_ended
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
internal fun ReadListeningUiActionCollector(uiActionFlow: Flow<ReadListeningUiAction>) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            ReadListeningUiAction.ShowSleepTimerEnded -> appSnackbarController.show(
                AppSnackbarMessage(
                    stringResource = Res.string.listening_sleep_timer_ended,
                    isDismissible = true,
                ),
            )
        }
    }
}
