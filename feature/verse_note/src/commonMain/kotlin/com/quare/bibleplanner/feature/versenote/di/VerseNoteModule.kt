package com.quare.bibleplanner.feature.versenote.di

import com.quare.bibleplanner.feature.versenote.presentation.VerseNoteViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val verseNoteModule = module {
    viewModelOf(::VerseNoteViewModel)
}
