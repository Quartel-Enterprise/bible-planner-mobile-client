package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.AppleSignInStarter
import com.quare.bibleplanner.feature.login.presentation.DefaultAppleSignInStarter
import com.quare.bibleplanner.feature.login.presentation.DefaultGoogleSignInStarter
import com.quare.bibleplanner.feature.login.presentation.GoogleSignInStarter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidLoginModule = module {
    factoryOf(::DefaultGoogleSignInStarter).bind<GoogleSignInStarter>()
    factoryOf(::DefaultAppleSignInStarter).bind<AppleSignInStarter>()
}
