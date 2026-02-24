package com.quare.bibleplanner.core.books.di

import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BibleMapper
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.mapper.VersionMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.books.data.repository.BibleRepositoryImpl
import com.quare.bibleplanner.core.books.data.repository.BibleVersionRepositoryImpl
import com.quare.bibleplanner.core.books.data.repository.BooksRepositoryImpl
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.AreAllPassagesReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksWithInformationBoxVisibilityUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBibleVersionsUseCase
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBibleVersionsUseCaseImpl
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeededUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsChapterReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsPassageReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ResetAllProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ToggleBookFavoriteUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateBookReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdatePassageReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateSpecificRangeChapterReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateWholeBookReadStatusIfNeededUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateWholeChapterReadStatusUseCase
import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val booksModule = module {
    // Data sources
    singleOf(::BooksLocalDataSource)
    factoryOf(::BibleVersionsRemoteDataSource)
    factoryOf(::BibleVersionsLocalDataSource)
    factoryOf(::VersionMapper)
    singleOf(::BibleVersionRepositoryImpl).bind<BibleVersionRepository>()
    factoryOf(::BibleMapper)

    // Providers
    singleOf(::BookMapsProvider)

    // Mappers
    factoryOf(::BooksWithChapterMapper)
    factoryOf(::FileNameToBookIdMapper)
    factoryOf(::BookGroupMapper)

    // Repository
    singleOf(::BooksRepositoryImpl).bind<BooksRepository>()
    singleOf(::BibleRepositoryImpl).bind<BibleRepository>()

    // Mappers
    factoryOf(::BookGroupMapper)

    // Use cases
    factoryOf(::InitializeBibleVersionsUseCaseImpl).bind<InitializeBibleVersionsUseCase>()
    factoryOf(::InitializeBooksIfNeededUseCase)
    factoryOf(::AreAllPassagesReadUseCase)
    factoryOf(::UpdateSpecificRangeChapterReadStatusUseCase)
    factoryOf(::GetSelectedVersionIdFlowUseCase)
    factoryOf(::GetSelectedBibleUseCase)
    factoryOf(::UpdatePassageReadStatusUseCase)
    factoryOf(::ResetAllProgressUseCase)
    factoryOf(::ToggleWholeChapterReadStatusUseCase)
    factoryOf(::CalculateBibleProgressUseCase)
    factoryOf(::GetBooksWithInformationBoxVisibilityUseCase)
    factoryOf(::ToggleBookFavoriteUseCase)
    factoryOf(::UpdateBookReadStatusUseCase)
    factoryOf(::UpdateWholeBookReadStatusIfNeededUseCase)
    factoryOf(::UpdateWholeChapterReadStatusUseCase)
    factoryOf(::IsChapterReadUseCase)
    factoryOf(::IsWholeChapterReadUseCase)
    factoryOf(::IsPassageReadUseCase)
}
