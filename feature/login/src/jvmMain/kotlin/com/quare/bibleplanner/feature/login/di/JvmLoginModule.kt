package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.domain.usecase.ApplySupabaseRedirectHtmlUseCase
import com.quare.bibleplanner.feature.login.presentation.AddGoogleAccountLauncher
import com.quare.bibleplanner.feature.login.presentation.DesktopAuthRedirectHtmlSynchronizer
import com.quare.bibleplanner.feature.login.presentation.GetDesktopAuthSuccessHtmlFlow
import com.quare.bibleplanner.feature.login.presentation.GetResourcesAsTextResult
import com.quare.bibleplanner.feature.login.presentation.JvmSignInStarter
import com.quare.bibleplanner.feature.login.presentation.NoGoogleAccountClassifier
import com.quare.bibleplanner.feature.login.presentation.SignInStarter
import com.quare.bibleplanner.feature.login.presentation.factory.DesktopAuthSuccessHtmlFactory
import com.quare.bibleplanner.feature.login.presentation.mapper.LanguageToDesktopAuthSuccessStringsMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val jvmLoginModule = module {
    factoryOf(::GetResourcesAsTextResult)
    factoryOf(::LanguageToDesktopAuthSuccessStringsMapper)
    factoryOf(::DesktopAuthSuccessHtmlFactory)
    factoryOf(::GetDesktopAuthSuccessHtmlFlow)
    factoryOf(::ApplySupabaseRedirectHtmlUseCase)
    factoryOf(::DesktopAuthRedirectHtmlSynchronizer)
    factoryOf(::JvmSignInStarter).bind<SignInStarter>()
    factory<NoGoogleAccountClassifier> { NoGoogleAccountClassifier { false } }
    factory<AddGoogleAccountLauncher> { AddGoogleAccountLauncher { } }
}
