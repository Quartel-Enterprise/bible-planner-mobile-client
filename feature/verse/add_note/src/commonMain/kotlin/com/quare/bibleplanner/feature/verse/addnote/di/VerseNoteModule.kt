package com.quare.bibleplanner.feature.verse.addnote.di

import com.quare.bibleplanner.feature.verse.addnote.presentation.VerseNoteViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val verseNoteModule = module {
    viewModelOf(::VerseNoteViewModel)
}
