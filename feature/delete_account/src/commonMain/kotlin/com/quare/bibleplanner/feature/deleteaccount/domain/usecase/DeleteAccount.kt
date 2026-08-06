package com.quare.bibleplanner.feature.deleteaccount.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface DeleteAccount {
    operator fun invoke(): Flow<DeleteAccountProgress>
}
