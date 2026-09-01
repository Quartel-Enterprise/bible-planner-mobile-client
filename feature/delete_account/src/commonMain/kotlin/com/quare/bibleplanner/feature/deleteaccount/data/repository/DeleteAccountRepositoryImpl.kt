package com.quare.bibleplanner.feature.deleteaccount.data.repository

import com.quare.bibleplanner.feature.deleteaccount.data.datasource.DeleteAccountRemoteDataSource
import com.quare.bibleplanner.feature.deleteaccount.domain.repository.DeleteAccountRepository

internal class DeleteAccountRepositoryImpl(
    private val remoteDataSource: DeleteAccountRemoteDataSource,
) : DeleteAccountRepository {
    override suspend fun deleteAccount() {
        remoteDataSource.deleteAccount()
    }
}
