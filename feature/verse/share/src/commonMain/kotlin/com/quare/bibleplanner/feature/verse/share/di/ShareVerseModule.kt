package com.quare.bibleplanner.feature.verse.share.di

import com.quare.bibleplanner.feature.verse.share.presentation.ShareVerseViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shareVerseModule = module {
    viewModelOf(::ShareVerseViewModel)
}
