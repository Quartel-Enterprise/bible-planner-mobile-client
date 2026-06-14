package com.quare.bibleplanner.core.books.di

import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BibleMapper
import com.quare.bibleplanner.core.books.data.mapper.BookFavoriteMapper
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.data.mapper.ChapterReadMapper
import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.mapper.VerseReadMapper
import com.quare.bibleplanner.core.books.data.mapper.VersionMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.books.data.repository.BibleRepositoryImpl
import com.quare.bibleplanner.core.books.data.repository.BibleVersionRepositoryImpl
import com.quare.bibleplanner.core.books.data.repository.BooksRepositoryImpl
import com.quare.bibleplanner.core.books.data.sync.ChapterReadLocalStore
import com.quare.bibleplanner.core.books.data.sync.ChapterReadRemoteStore
import com.quare.bibleplanner.core.books.data.sync.FavoritesLocalStore
import com.quare.bibleplanner.core.books.data.sync.FavoritesRemoteStore
import com.quare.bibleplanner.core.books.data.sync.VerseReadLocalStore
import com.quare.bibleplanner.core.books.data.sync.VerseReadRemoteStore
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.AreAllPassagesReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ClearLocalReadingDataUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetBookByIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksWithInformationBoxVisibilityUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleNameFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesWithTextsByChapterIdFlowUseCase
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
import com.quare.bibleplanner.core.sync.data.OfflineFirstSynchronizer
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val booksModule = module {
    // Data sources
    singleOf(::BooksLocalDataSource)
    factoryOf(::BibleVersionsRemoteDataSource)
    factoryOf(::BibleVersionsLocalDataSource)
    factoryOf(::VersionMapper)
    factoryOf(::BookFavoriteMapper)
    factoryOf(::FavoritesLocalStore)
    factoryOf(::FavoritesRemoteStore)
    factoryOf(::ChapterReadMapper)
    factoryOf(::ChapterReadLocalStore)
    factoryOf(::ChapterReadRemoteStore)
    factoryOf(::VerseReadMapper)
    factoryOf(::VerseReadLocalStore)
    factoryOf(::VerseReadRemoteStore)
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

    // Sync
    single<Synchronizer>(named("favoritesSync")) {
        OfflineFirstSynchronizer(
            localStore = get<FavoritesLocalStore>(),
            remoteStore = get<FavoritesRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            logTag = "FavoritesSync",
        )
    }
    single<Synchronizer>(named("chapterReadSync")) {
        OfflineFirstSynchronizer(
            localStore = get<ChapterReadLocalStore>(),
            remoteStore = get<ChapterReadRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            logTag = "ChapterReadSync",
        )
    }
    single<Synchronizer>(named("verseReadSync")) {
        OfflineFirstSynchronizer(
            localStore = get<VerseReadLocalStore>(),
            remoteStore = get<VerseReadRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            logTag = "VerseReadSync",
        )
    }
    factoryOf(::ClearLocalReadingDataUseCase)

    // Mappers
    factoryOf(::BookGroupMapper)

    // Use cases
    factoryOf(::InitializeBibleVersionsUseCaseImpl).bind<InitializeBibleVersionsUseCase>()
    factoryOf(::InitializeBooksIfNeededUseCase)
    factoryOf(::AreAllPassagesReadUseCase)
    factoryOf(::UpdateSpecificRangeChapterReadStatusUseCase)
    factoryOf(::GetBookByIdFlowUseCase)
    factoryOf(::GetVersesWithTextsByChapterIdFlowUseCase)
    factoryOf(::GetSelectedVersionIdFlowUseCase)
    factoryOf(::GetSelectedBibleFlowUseCase)
    factoryOf(::GetSelectedBibleNameFlowUseCase)
    factoryOf(::UpdatePassageReadStatusUseCase)
    factoryOf(::ResetAllProgressUseCase)
    factoryOf(::ToggleWholeChapterReadStatusUseCase)
    factoryOf(::CalculateBibleProgressUseCase)
    factoryOf(::GetBooksWithInformationBoxVisibilityUseCase)
    factoryOf(::GetChapterIdUseCase)
    factoryOf(::ToggleBookFavoriteUseCase)
    factoryOf(::UpdateBookReadStatusUseCase)
    factoryOf(::UpdateWholeBookReadStatusIfNeededUseCase)
    factoryOf(::UpdateWholeChapterReadStatusUseCase)
    factoryOf(::IsChapterReadUseCase)
    factoryOf(::IsWholeChapterReadUseCase)
    factoryOf(::IsPassageReadUseCase)
}
