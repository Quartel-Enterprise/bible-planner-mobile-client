package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.AddGoogleAccountLauncher
import com.quare.bibleplanner.feature.login.presentation.AndroidAddGoogleAccountLauncher
import com.quare.bibleplanner.feature.login.presentation.AndroidNoGoogleAccountClassifier
import com.quare.bibleplanner.feature.login.presentation.DefaultSignInStarter
import com.quare.bibleplanner.feature.login.presentation.NoGoogleAccountClassifier
import com.quare.bibleplanner.feature.login.presentation.SignInStarter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidLoginModule = module {
    factoryOf(::DefaultSignInStarter).bind<SignInStarter>()
    factoryOf(::AndroidNoGoogleAccountClassifier).bind<NoGoogleAccountClassifier>()
    factory<AddGoogleAccountLauncher> { AndroidAddGoogleAccountLauncher(androidContext()) }
}
