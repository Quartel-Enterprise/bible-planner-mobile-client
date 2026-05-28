package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.DefaultGoogleSignInStarter
import com.quare.bibleplanner.feature.login.presentation.GoogleSignInStarter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val iosLoginModule = module {
    factoryOf(::DefaultGoogleSignInStarter).bind<GoogleSignInStarter>()
}
