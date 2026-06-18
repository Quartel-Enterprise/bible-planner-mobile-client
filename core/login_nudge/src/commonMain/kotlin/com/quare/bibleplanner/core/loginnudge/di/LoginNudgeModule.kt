package com.quare.bibleplanner.core.loginnudge.di

import com.quare.bibleplanner.core.loginnudge.data.LoginNudgePreferencesImpl
import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences
import com.quare.bibleplanner.core.loginnudge.domain.usecase.DismissLoginNudgePermanently
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.loginnudge.domain.usecase.ShouldShowLoginNudge
import com.quare.bibleplanner.core.loginnudge.domain.usecase.SnoozeLoginNudge
import com.quare.bibleplanner.core.loginnudge.domain.usecase.impl.DismissLoginNudgePermanentlyUseCase
import com.quare.bibleplanner.core.loginnudge.domain.usecase.impl.RequestLoginNudgeIfNeededUseCase
import com.quare.bibleplanner.core.loginnudge.domain.usecase.impl.ShouldShowLoginNudgeUseCase
import com.quare.bibleplanner.core.loginnudge.domain.usecase.impl.SnoozeLoginNudgeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val loginNudgeModule = module {
    // Data
    singleOf(::LoginNudgePreferencesImpl).bind<LoginNudgePreferences>()

    // Domain
    factoryOf(::ShouldShowLoginNudgeUseCase).bind<ShouldShowLoginNudge>()
    factoryOf(::RequestLoginNudgeIfNeededUseCase).bind<RequestLoginNudgeIfNeeded>()
    factoryOf(::SnoozeLoginNudgeUseCase).bind<SnoozeLoginNudge>()
    factoryOf(::DismissLoginNudgePermanentlyUseCase).bind<DismissLoginNudgePermanently>()
}
