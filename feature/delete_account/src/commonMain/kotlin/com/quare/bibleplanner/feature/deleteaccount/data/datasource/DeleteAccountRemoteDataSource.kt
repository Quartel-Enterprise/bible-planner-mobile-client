package com.quare.bibleplanner.feature.deleteaccount.data.datasource

import io.github.jan.supabase.functions.Functions
import io.ktor.http.isSuccess

internal class DeleteAccountRemoteDataSource(
    private val functions: Functions,
) {
    suspend fun deleteAccount() {
        val response = functions.invoke(FUNCTION_DELETE_ACCOUNT)
        if (!response.status.isSuccess()) {
            throw DeleteAccountRequestException(response.status.value)
        }
    }

    private companion object {
        const val FUNCTION_DELETE_ACCOUNT = "delete-account"
    }
}
