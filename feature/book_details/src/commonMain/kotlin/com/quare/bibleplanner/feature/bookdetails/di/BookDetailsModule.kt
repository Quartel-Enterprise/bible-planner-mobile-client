package com.quare.bibleplanner.feature.bookdetails.di

import com.quare.bibleplanner.feature.bookdetails.presentation.viewmodel.BookDetailsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bookDetailsModule = module {
    viewModelOf(::BookDetailsViewModel)
}
