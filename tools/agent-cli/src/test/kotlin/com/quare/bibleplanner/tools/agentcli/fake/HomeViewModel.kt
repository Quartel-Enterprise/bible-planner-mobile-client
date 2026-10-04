package com.quare.bibleplanner.tools.agentcli.fake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource

internal class HomeViewModel(
    private val navigator: Navigator,
    private val snackbarController: AppSnackbarController,
) : ViewModel() {
    val uiState: StateFlow<String>
        field = MutableStateFlow("home")

    @OptIn(InternalResourceApi::class)
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnDayClick -> navigator.navigate(
                DayNavRoute(
                    dayNumber = event.dayNumber,
                    weekNumber = 1,
                    readingPlanType = "BOOKS",
                ),
            )

            HomeUiEvent.OnSlowSaveClick -> viewModelScope.launch {
                delay(30)
                uiState.value = "saved"
            }

            HomeUiEvent.OnSnackbarClick -> snackbarController.show(
                AppSnackbarMessage(
                    stringResource = StringResource(
                        id = "string:saved",
                        key = "saved",
                        items = emptySet(),
                    ),
                    isDismissible = true,
                ),
            )
        }
    }
}
