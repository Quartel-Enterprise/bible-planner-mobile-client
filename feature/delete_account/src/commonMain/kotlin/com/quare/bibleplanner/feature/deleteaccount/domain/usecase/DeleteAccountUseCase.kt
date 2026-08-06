package com.quare.bibleplanner.feature.deleteaccount.domain.usecase

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.deleteaccount.domain.repository.DeleteAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteAccountUseCase(
    private val repository: DeleteAccountRepository,
    private val closeDeletedAccountSession: CloseDeletedAccountSession,
) : DeleteAccount {
    override fun invoke(): Flow<DeleteAccountProgress> = flow {
        emit(DeleteAccountProgress.InProgress(DeleteAccountPhase.DELETING_DATA))
        val deletion = suspendRunCatching { repository.deleteAccount() }
        deletion.onFailure {
            emit(DeleteAccountProgress.Finished(Result.failure(it)))
            return@flow
        }
        emit(DeleteAccountProgress.InProgress(DeleteAccountPhase.CLOSING_ACCOUNT))
        emit(DeleteAccountProgress.Finished(closeDeletedAccountSession()))
    }
}
