package com.quare.bibleplanner.core.verseannotations.di

import com.quare.bibleplanner.core.sync.data.OfflineFirstSynchronizer
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import com.quare.bibleplanner.core.verseannotations.data.mapper.SavedVerseMapper
import com.quare.bibleplanner.core.verseannotations.data.mapper.SyncTimestampMapper
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseHighlightMapper
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseNoteEntityMapper
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseNoteSyncMapper
import com.quare.bibleplanner.core.verseannotations.data.repository.HighlightPaletteRepositoryImpl
import com.quare.bibleplanner.core.verseannotations.data.repository.SavedVerseRepositoryImpl
import com.quare.bibleplanner.core.verseannotations.data.repository.VerseHighlightRepositoryImpl
import com.quare.bibleplanner.core.verseannotations.data.repository.VerseNoteRepositoryImpl
import com.quare.bibleplanner.core.verseannotations.data.repository.VerseSelectionRepositoryImpl
import com.quare.bibleplanner.core.verseannotations.data.sync.SavedVerseLocalStore
import com.quare.bibleplanner.core.verseannotations.data.sync.SavedVerseRemoteStore
import com.quare.bibleplanner.core.verseannotations.data.sync.VerseHighlightLocalStore
import com.quare.bibleplanner.core.verseannotations.data.sync.VerseHighlightRemoteStore
import com.quare.bibleplanner.core.verseannotations.data.sync.VerseNoteLocalStore
import com.quare.bibleplanner.core.verseannotations.data.sync.VerseNoteRemoteStore
import com.quare.bibleplanner.core.verseannotations.domain.repository.HighlightPaletteRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.SavedVerseRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseSelectionRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.AddCustomHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ApplyHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ClearVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.DeleteVerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.GetVerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveHighlightPalette
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.RemoveCustomHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.SaveVerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleSavedVerses
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.AddCustomHighlightColorUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ApplyHighlightColorUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ClearVerseSelectionUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.DeleteVerseNoteUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.GetVerseNoteUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveChapterAnnotationsUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveHighlightPaletteUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveVerseSelectionUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.RemoveCustomHighlightColorUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.SaveVerseNoteUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ToggleSavedVersesUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ToggleVerseSelectionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val verseAnnotationsModule = module {
    factoryOf(::SyncTimestampMapper)
    factoryOf(::VerseHighlightMapper)
    factoryOf(::SavedVerseMapper)
    factoryOf(::VerseNoteEntityMapper)
    factoryOf(::VerseNoteSyncMapper)

    singleOf(::VerseHighlightRepositoryImpl).bind<VerseHighlightRepository>()
    singleOf(::SavedVerseRepositoryImpl).bind<SavedVerseRepository>()
    singleOf(::VerseNoteRepositoryImpl).bind<VerseNoteRepository>()
    singleOf(::HighlightPaletteRepositoryImpl).bind<HighlightPaletteRepository>()
    singleOf(::VerseSelectionRepositoryImpl).bind<VerseSelectionRepository>()

    factoryOf(::ObserveChapterAnnotationsUseCase).bind<ObserveChapterAnnotations>()
    factoryOf(::ApplyHighlightColorUseCase).bind<ApplyHighlightColor>()
    factoryOf(::ToggleSavedVersesUseCase).bind<ToggleSavedVerses>()
    factoryOf(::SaveVerseNoteUseCase).bind<SaveVerseNote>()
    factoryOf(::GetVerseNoteUseCase).bind<GetVerseNote>()
    factoryOf(::DeleteVerseNoteUseCase).bind<DeleteVerseNote>()
    factoryOf(::ObserveHighlightPaletteUseCase).bind<ObserveHighlightPalette>()
    factoryOf(::AddCustomHighlightColorUseCase).bind<AddCustomHighlightColor>()
    factoryOf(::RemoveCustomHighlightColorUseCase).bind<RemoveCustomHighlightColor>()
    factoryOf(::ObserveVerseSelectionUseCase).bind<ObserveVerseSelection>()
    factoryOf(::ToggleVerseSelectionUseCase).bind<ToggleVerseSelection>()
    factoryOf(::ClearVerseSelectionUseCase).bind<ClearVerseSelection>()

    factoryOf(::VerseHighlightLocalStore)
    factoryOf(::VerseHighlightRemoteStore)
    factoryOf(::SavedVerseLocalStore)
    factoryOf(::SavedVerseRemoteStore)
    factoryOf(::VerseNoteLocalStore)
    factoryOf(::VerseNoteRemoteStore)

    single<Synchronizer>(named("verseHighlightSync")) {
        OfflineFirstSynchronizer(
            localStore = get<VerseHighlightLocalStore>(),
            remoteStore = get<VerseHighlightRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            currentTimestampProvider = get(),
            logTag = "VerseHighlightSync",
        )
    }
    single<Synchronizer>(named("savedVerseSync")) {
        OfflineFirstSynchronizer(
            localStore = get<SavedVerseLocalStore>(),
            remoteStore = get<SavedVerseRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            currentTimestampProvider = get(),
            logTag = "SavedVerseSync",
        )
    }
    single<Synchronizer>(named("verseNoteSync")) {
        OfflineFirstSynchronizer(
            localStore = get<VerseNoteLocalStore>(),
            remoteStore = get<VerseNoteRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            currentTimestampProvider = get(),
            logTag = "VerseNoteSync",
        )
    }
}
