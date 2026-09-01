package com.quare.bibleplanner.feature.deleteaccount.di

import com.quare.bibleplanner.feature.deleteaccount.data.datasource.DeleteAccountRemoteDataSource
import com.quare.bibleplanner.feature.deleteaccount.data.repository.DeleteAccountRepositoryImpl
import com.quare.bibleplanner.feature.deleteaccount.domain.repository.DeleteAccountRepository
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.CloseDeletedAccountSession
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.CloseDeletedAccountSessionUseCase
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccount
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountUseCase
import com.quare.bibleplanner.feature.deleteaccount.presentation.mapper.StoreNameMapper
import com.quare.bibleplanner.feature.deleteaccount.presentation.provider.GetConfirmationKeyword
import com.quare.bibleplanner.feature.deleteaccount.presentation.provider.GetConfirmationKeywordImpl
import com.quare.bibleplanner.feature.deleteaccount.presentation.viewmodel.DeleteAccountViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val deleteAccountModule = module {
    singleOf(::DeleteAccountRemoteDataSource)
    singleOf(::DeleteAccountRepositoryImpl).bind<DeleteAccountRepository>()
    factoryOf(::CloseDeletedAccountSessionUseCase).bind<CloseDeletedAccountSession>()
    factoryOf(::DeleteAccountUseCase).bind<DeleteAccount>()
    factoryOf(::StoreNameMapper)
    factoryOf(::GetConfirmationKeywordImpl).bind<GetConfirmationKeyword>()
    viewModelOf(::DeleteAccountViewModel)
}
