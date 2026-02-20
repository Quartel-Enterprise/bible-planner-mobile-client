package com.quare.bibleplanner.feature.bibleversion.di

import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacadeImpl
import com.quare.bibleplanner.feature.bibleversion.domain.DownloadBibleUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsByLanguageUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.SetSelectedVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.BibleVersionViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val bibleVersionModule = module {
    // Data
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

    // Domain
    singleOf(::BibleVersionDownloaderFacadeImpl).bind<BibleVersionDownloaderFacade>()
    factoryOf(::GetBibleVersionsByLanguageUseCase)
    factoryOf(::SupabaseBookAbbreviationMapper)
    singleOf(::DownloadBibleUseCase)
    factoryOf(::SetSelectedVersionUseCase)

    // Presentation
    viewModelOf(::BibleVersionViewModel)
}
