package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.DefaultSignInStarter
import com.quare.bibleplanner.feature.login.presentation.SignInStarter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidLoginModule = module {
    factoryOf(::DefaultSignInStarter).bind<SignInStarter>()
}
