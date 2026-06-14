package com.quare.bibleplanner.core.user.di

import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserIdUseCase
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserIdUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val userModule = module {
    factoryOf(::SessionUserMapper)
    factoryOf(::ObserveAuthenticatedUserIdUseCase).bind<ObserveAuthenticatedUserId>()
    factoryOf(::GetAuthenticatedUserIdUseCase).bind<GetAuthenticatedUserId>()
}
