package com.quare.bibleplanner.feature.deleteaccount.domain.repository

fun interface DeleteAccountRepository {
    suspend fun deleteAccount()
}
