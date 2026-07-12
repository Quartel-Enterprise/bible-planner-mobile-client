package com.quare.bibleplanner.feature.accountdetails.di

import com.quare.bibleplanner.feature.accountdetails.presentation.mapper.DeviceUiModelMapper
import com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel.AccountDetailsViewModel
import com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel.RenameDeviceViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val accountDetailsModule = module {
    factoryOf(::DeviceUiModelMapper)
    viewModelOf(::AccountDetailsViewModel)
    viewModelOf(::RenameDeviceViewModel)
}
