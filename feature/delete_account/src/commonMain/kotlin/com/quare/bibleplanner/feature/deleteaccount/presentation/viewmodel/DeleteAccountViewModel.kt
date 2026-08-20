package com.quare.bibleplanner.feature.deleteaccount.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.delete_account.generated.resources.Res
import bibleplanner.feature.delete_account.generated.resources.delete_account_error_message
import bibleplanner.feature.delete_account.generated.resources.delete_account_success_message
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccount
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountPhase
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountProgress
import com.quare.bibleplanner.feature.deleteaccount.presentation.mapper.StoreNameMapper
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiAction
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiEvent
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiState
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiStatus
import com.quare.bibleplanner.feature.deleteaccount.presentation.provider.GetConfirmationKeyword
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class DeleteAccountViewModel(
    private val deleteAccount: DeleteAccount,
    getConfirmationKeyword: GetConfirmationKeyword,
    getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase,
    private val storeNameMapper: StoreNameMapper,
    trackEvent: TrackEvent,
) : TrackedViewModel<DeleteAccountUiEvent>(trackEvent) {
    private val logger = Logger.withTag(LOG_TAG)

    private val successFeedbackDuration: Duration = 700.milliseconds

    private var confirmationKeyword: String? = null

    private val _uiState: MutableStateFlow<DeleteAccountUiState> = MutableStateFlow(
        DeleteAccountUiState(
            status = DeleteAccountUiStatus.Idle,
            confirmationText = "",
            isConfirmEnabled = false,
            isProUser = false,
            storeNameRes = null,
        ),
    )
    val uiState: StateFlow<DeleteAccountUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<DeleteAccountUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DeleteAccountUiAction> = _uiAction

    init {
        viewModelScope.launch {
            confirmationKeyword = getConfirmationKeyword()
            _uiState.update { it.copy(isConfirmEnabled = it.confirmationText.matchesKeyword()) }
        }
        getSubscriptionStatusFlow()
            .onEach { status -> _uiState.update { it.withSubscription(status) } }
            .launchIn(viewModelScope)
    }

    override fun handleEvent(event: DeleteAccountUiEvent) {
        when (event) {
            is DeleteAccountUiEvent.OnConfirmationTextChange -> updateConfirmationText(event.text)
            DeleteAccountUiEvent.OnConfirmDelete -> performDeletion()
            DeleteAccountUiEvent.OnCancel -> navigateBack()
            DeleteAccountUiEvent.OnDismiss -> if (isIdle) navigateBack()
        }
    }

    private fun DeleteAccountUiState.withSubscription(status: SubscriptionStatus?): DeleteAccountUiState {
        val pro = status as? SubscriptionStatus.Pro
        return copy(
            isProUser = pro != null,
            storeNameRes = pro?.let { storeNameMapper.map(it.store) },
        )
    }

    private val isIdle: Boolean
        get() = _uiState.value.status == DeleteAccountUiStatus.Idle

    private fun updateConfirmationText(text: String) {
        _uiState.update {
            it.copy(
                confirmationText = text,
                isConfirmEnabled = text.matchesKeyword(),
            )
        }
    }

    private fun performDeletion() {
        val state = _uiState.value
        if (!state.isConfirmEnabled || !isIdle) return
        trackEvent(
            name = AnalyticsEventNames.ACCOUNT_DELETE_CONFIRMED,
            params = mapOf(AnalyticsParams.IS_PRO to state.isProUser),
        )
        deleteAccount()
            .onEach { progress ->
                when (progress) {
                    is DeleteAccountProgress.InProgress ->
                        _uiState.update { it.copy(status = DeleteAccountUiStatus.Deleting(progress.phase)) }

                    is DeleteAccountProgress.Finished -> progress.result.handleDeletion()
                }
            }.launchIn(viewModelScope)
    }

    private suspend fun Result<Unit>.handleDeletion() {
        onSuccess {
            _uiState.update { it.copy(status = DeleteAccountUiStatus.Deleted) }
            delay(successFeedbackDuration)
            _uiAction.emit(DeleteAccountUiAction.NotifySuccess(Res.string.delete_account_success_message))
            _uiAction.emit(DeleteAccountUiAction.NavigateBack)
        }.onFailure { throwable ->
            val reason = getFailureReason()
            logger.e(throwable) { "Account deletion failed during $reason" }
            trackEvent(
                name = AnalyticsEventNames.ACCOUNT_DELETE_FAILED,
                params = mapOf(AnalyticsParams.REASON to reason),
            )
            _uiState.update { it.copy(status = DeleteAccountUiStatus.Idle) }
            _uiAction.emit(DeleteAccountUiAction.ShowSnackbar(Res.string.delete_account_error_message))
        }
    }

    private fun getFailureReason(): String {
        val status = _uiState.value.status
        return if (status is DeleteAccountUiStatus.Deleting && status.phase == DeleteAccountPhase.CLOSING_ACCOUNT) {
            REASON_CLOSE_SESSION
        } else {
            REASON_DELETE_REQUEST
        }
    }

    private fun String.matchesKeyword(): Boolean {
        val keyword = confirmationKeyword ?: return false
        return trim().equals(keyword, ignoreCase = true)
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiAction.emit(DeleteAccountUiAction.NavigateBack)
        }
    }

    private companion object {
        const val LOG_TAG = "DeleteAccount"
        const val REASON_DELETE_REQUEST = "delete_request"
        const val REASON_CLOSE_SESSION = "close_session"
    }
}
