package com.quare.bibleplanner.feature.addnotesfreewarning.di

import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel.AddNotesFreeWarningViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val addNotesFreeWarningModule = module {
    // Presentation
    viewModelOf(::AddNotesFreeWarningViewModel)
}
