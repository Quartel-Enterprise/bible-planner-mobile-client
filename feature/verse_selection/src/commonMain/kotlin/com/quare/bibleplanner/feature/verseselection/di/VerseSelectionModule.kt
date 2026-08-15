package com.quare.bibleplanner.feature.verseselection.di

import com.quare.bibleplanner.feature.verseselection.presentation.VerseSelectionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val verseSelectionModule = module {
    viewModelOf(::VerseSelectionViewModel)
}
