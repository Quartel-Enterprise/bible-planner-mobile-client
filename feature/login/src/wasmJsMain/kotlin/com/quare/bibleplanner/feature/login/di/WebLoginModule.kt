package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.AddGoogleAccountLauncher
import com.quare.bibleplanner.feature.login.presentation.IsGoogleCredentialUnavailable
import com.quare.bibleplanner.feature.login.presentation.SignInStarter
import com.quare.bibleplanner.feature.login.presentation.WebSignInStarter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val webLoginModule = module {
    factoryOf(::WebSignInStarter).bind<SignInStarter>()
    factory<IsGoogleCredentialUnavailable> { IsGoogleCredentialUnavailable { false } }
    factory<AddGoogleAccountLauncher> { AddGoogleAccountLauncher { } }
}
