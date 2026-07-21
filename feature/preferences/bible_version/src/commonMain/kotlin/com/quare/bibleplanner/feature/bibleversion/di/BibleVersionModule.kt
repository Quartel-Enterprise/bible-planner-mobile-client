package com.quare.bibleplanner.feature.bibleversion.di

import com.quare.bibleplanner.core.provider.supabase.CONTENT_BUCKET
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.domain.DownloadBibleUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadBooksInParallelUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadChaptersUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsByLanguageUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetNewTestamentIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPentateuchIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPrioritizedBookIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.SetSelectedVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.BibleVersionViewModel
import com.quare.bibleplanner.feature.bibleversion.presentation.factory.BibleVersionsUiStateFactory
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val bibleVersionModule = module {
    // Data
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

    // Domain
    factoryOf(::GetBibleVersionsByLanguageUseCase)
    factoryOf(::SupabaseBookAbbreviationMapper)
    singleOf(::DownloadBibleUseCase)
    factoryOf(::SetSelectedVersionUseCase)
    factoryOf(::GetNewTestamentIdsUseCase)
    factoryOf(::GetPentateuchIdsUseCase)
    factoryOf(::GetPrioritizedBookIdsUseCase)
    factory {
        DownloadChaptersUseCase(
            supabaseBookAbbreviationMapper = get(),
            chapterDao = get(),
            verseDao = get(),
            bucketApi = get(named(CONTENT_BUCKET)),
        )
    }
    factoryOf(::DownloadBooksInParallelUseCase)
    factoryOf(::PauseBibleVersionDownloadUseCase)
    factoryOf(::DeleteBibleVersionDownloadUseCase)
    factoryOf(::BibleVersionsUiStateFactory)

    // Presentation
    viewModelOf(::BibleVersionViewModel)
}
