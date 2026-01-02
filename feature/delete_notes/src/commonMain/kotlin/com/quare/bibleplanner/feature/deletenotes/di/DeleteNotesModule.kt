package com.quare.bibleplanner.feature.deletenotes.di

import com.quare.bibleplanner.feature.deletenotes.presentation.viewmodel.DeleteNotesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val deleteNotesModule = module {
    // Presentation
    viewModelOf(::DeleteNotesViewModel)
}
