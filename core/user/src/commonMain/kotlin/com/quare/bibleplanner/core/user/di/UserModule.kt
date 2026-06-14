package com.quare.bibleplanner.core.user.di

import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserIdUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val userModule = module {
    factoryOf(::SessionUserMapper)
    factoryOf(::ObserveAuthenticatedUserIdUseCase).bind<ObserveAuthenticatedUserId>()
}
