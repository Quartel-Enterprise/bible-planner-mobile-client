package com.quare.bibleplanner.core.user.di

import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserIdUseCase
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserIdUseCase
import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUserUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val userModule = module {
    factoryOf(::SessionUserMapper)
    factoryOf(::ObserveAuthenticatedUserIdUseCase).bind<ObserveAuthenticatedUserId>()
    factoryOf(::ObserveCurrentUserUseCase).bind<ObserveCurrentUser>()
    factoryOf(::GetAuthenticatedUserIdUseCase).bind<GetAuthenticatedUserId>()
}
