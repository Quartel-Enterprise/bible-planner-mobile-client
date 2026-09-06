package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.AddGoogleAccountLauncher
import com.quare.bibleplanner.feature.login.presentation.AndroidAddGoogleAccountLauncher
import com.quare.bibleplanner.feature.login.presentation.AndroidIsGoogleCredentialUnavailable
import com.quare.bibleplanner.feature.login.presentation.DefaultSignInStarter
import com.quare.bibleplanner.feature.login.presentation.IsGoogleCredentialUnavailable
import com.quare.bibleplanner.feature.login.presentation.SignInStarter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidLoginModule = module {
    factoryOf(::DefaultSignInStarter).bind<SignInStarter>()
    factoryOf(::AndroidIsGoogleCredentialUnavailable).bind<IsGoogleCredentialUnavailable>()
    factory<AddGoogleAccountLauncher> { AndroidAddGoogleAccountLauncher(androidContext()) }
}
