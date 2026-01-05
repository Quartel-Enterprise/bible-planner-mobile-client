package com.quare.bibleplanner.feature.more.di

import com.quare.bibleplanner.feature.more.presentation.factory.MoreUiStateFactory
import com.quare.bibleplanner.feature.more.presentation.viewmodel.MoreViewModel
import com.quare.bibleplanner.feature.more.domain.usecase.ShouldShowDonateOptionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moreModule = module {
    // Domain
    factoryOf(::ShouldShowDonateOptionUseCase)

    // Presentation
    factoryOf(::MoreUiStateFactory)
    viewModelOf(::MoreViewModel)
}
