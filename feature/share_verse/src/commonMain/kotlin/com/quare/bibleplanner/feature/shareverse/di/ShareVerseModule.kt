package com.quare.bibleplanner.feature.shareverse.di

import com.quare.bibleplanner.feature.shareverse.presentation.ShareVerseViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shareVerseModule = module {
    viewModelOf(::ShareVerseViewModel)
}
