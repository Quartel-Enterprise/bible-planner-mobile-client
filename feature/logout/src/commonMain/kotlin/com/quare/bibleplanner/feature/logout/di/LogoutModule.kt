package com.quare.bibleplanner.feature.logout.di

import com.quare.bibleplanner.feature.logout.domain.usecase.EndSession
import com.quare.bibleplanner.feature.logout.domain.usecase.EndSessionUseCase
import com.quare.bibleplanner.feature.logout.domain.usecase.FlushPendingChangesUseCase
import com.quare.bibleplanner.feature.logout.domain.usecase.HandleCurrentDeviceRevoked
import com.quare.bibleplanner.feature.logout.domain.usecase.HandleCurrentDeviceRevokedUseCase
import com.quare.bibleplanner.feature.logout.domain.usecase.Logout
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutUseCase
import com.quare.bibleplanner.feature.logout.domain.usecase.ObserveSessionLoss
import com.quare.bibleplanner.feature.logout.domain.usecase.ObserveSessionLossUseCase
import com.quare.bibleplanner.feature.logout.presentation.mapper.LogoutErrorMapper
import com.quare.bibleplanner.feature.logout.presentation.viewmodel.LogoutViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

val logoutModule = module {
    factory {
        FlushPendingChangesUseCase(
            pushAllPending = get(),
            flushTimeout = 5.seconds,
        )
    }
    factoryOf(::EndSessionUseCase).bind<EndSession>()
    factoryOf(::LogoutUseCase).bind<Logout>()
    factoryOf(::ObserveSessionLossUseCase).bind<ObserveSessionLoss>()
    factoryOf(::HandleCurrentDeviceRevokedUseCase).bind<HandleCurrentDeviceRevoked>()
    factoryOf(::LogoutErrorMapper)
    viewModelOf(::LogoutViewModel)
}
