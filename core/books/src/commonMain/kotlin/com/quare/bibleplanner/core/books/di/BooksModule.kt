package com.quare.bibleplanner.core.books.di

import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.repository.BooksRepositoryImpl
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeeded
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val booksModule = module {
    // Data sources
    singleOf(::BooksLocalDataSource)

    // Mappers
    factoryOf(::BooksWithChapterMapper)
    factoryOf(::FileNameToBookIdMapper)

    // Repository
    singleOf(::BooksRepositoryImpl).bind<BooksRepository>()

    // Use cases
    factoryOf(::InitializeBooksIfNeeded)
}
