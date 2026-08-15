package com.quare.bibleplanner.feature.verse.selectionmenu.di

import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.VerseSelectionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val verseSelectionModule = module {
    viewModelOf(::VerseSelectionViewModel)
}
