package com.quare.bibleplanner.feature.bibleversion.di

import com.quare.bibleplanner.feature.bibleversion.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.feature.bibleversion.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.feature.bibleversion.data.mapper.BibleVersionMapper
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.data.mapper.VersionMapper
import com.quare.bibleplanner.feature.bibleversion.data.repository.BibleVersionMetadataRepositoryImpl
import com.quare.bibleplanner.feature.bibleversion.data.repository.BibleVersionRepositoryImpl
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacadeImpl
import com.quare.bibleplanner.feature.bibleversion.domain.DownloadBibleUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionMetadataRepository
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.InitializeBibleVersionsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.InitializeBibleVersionsUseCaseImpl
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
    factoryOf(::BibleVersionsRemoteDataSource)
    factoryOf(::BibleVersionsLocalDataSource)
    factoryOf(::VersionMapper)
    singleOf(::BibleVersionMetadataRepositoryImpl).bind<BibleVersionMetadataRepository>()
    singleOf(::BibleVersionRepositoryImpl).bind<BibleVersionRepository>()
    factoryOf(::BibleVersionMapper)

    // Domain
    factoryOf(::InitializeBibleVersionsUseCaseImpl).bind<InitializeBibleVersionsUseCase>()
    factoryOf(::GetBibleVersionsUseCase)
    factoryOf(::SupabaseBookAbbreviationMapper)
    singleOf(::DownloadBibleUseCase)
    singleOf(::BibleVersionDownloaderFacadeImpl).bind<BibleVersionDownloaderFacade>()

    // Presentation
    viewModelOf(::BibleVersionViewModel)
}
