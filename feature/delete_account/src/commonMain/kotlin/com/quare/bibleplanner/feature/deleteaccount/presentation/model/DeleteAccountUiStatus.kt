package com.quare.bibleplanner.feature.deleteaccount.presentation.model

import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountPhase

internal sealed interface DeleteAccountUiStatus {
    data object Idle : DeleteAccountUiStatus

    data class Deleting(
        val phase: DeleteAccountPhase,
    ) : DeleteAccountUiStatus

    data object Deleted : DeleteAccountUiStatus
}
