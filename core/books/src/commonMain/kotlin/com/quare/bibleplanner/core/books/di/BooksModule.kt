package com.quare.bibleplanner.core.books.di

import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.books.data.repository.BooksRepositoryImpl
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksWithInformationBoxVisibilityUseCase
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeeded
import com.quare.bibleplanner.core.books.domain.usecase.MarkPassagesReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ResetAllProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ToggleBookFavoriteUseCase
import com.quare.bibleplanner.core.books.presentation.mapper.BookCategorizationMapper
import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val booksModule = module {
    // Data sources
    singleOf(::BooksLocalDataSource)

    // Providers
    singleOf(::BookMapsProvider)

    // Mappers
    factoryOf(::BooksWithChapterMapper)
    factoryOf(::FileNameToBookIdMapper)
    factoryOf(::BookGroupMapper)

    // Repository
    singleOf(::BooksRepositoryImpl).bind<BooksRepository>()

    // Mappers
    factoryOf(::BookGroupMapper)
    factoryOf(::BookCategorizationMapper)

    // Use cases
    factoryOf(::InitializeBooksIfNeeded)
    factoryOf(::MarkPassagesReadUseCase)
    factoryOf(::ResetAllProgressUseCase)
    factoryOf(::CalculateBibleProgressUseCase)
    factoryOf(::GetBooksWithInformationBoxVisibilityUseCase)
    factoryOf(::ToggleBookFavoriteUseCase)
}
