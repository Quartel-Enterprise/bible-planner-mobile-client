package com.quare.bibleplanner.feature.books.di

import com.quare.bibleplanner.feature.books.presentation.viewmodel.BooksViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureBooksModule = module {
    viewModelOf(::BooksViewModel)
}
