package com.quare.bibleplanner.feature.more.di

import com.quare.bibleplanner.feature.more.presentation.viewmodel.MoreViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val moreModule = module {
    viewModel {
        MoreViewModel(
            isFreeUser = get(),
            isInstagramLinkVisible = get(),
            calculateBibleProgress = get(),
        )
    }
}
