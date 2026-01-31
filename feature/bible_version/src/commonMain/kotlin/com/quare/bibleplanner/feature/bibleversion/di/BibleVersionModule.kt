package com.quare.bibleplanner.feature.bibleversion.di

import com.quare.bibleplanner.feature.bibleversion.data.remote.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.data.repository.BibleVersionRepositoryImpl
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacadeImpl
import com.quare.bibleplanner.feature.bibleversion.domain.DownloadBibleUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.BibleVersionViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val bibleVersionModule = module {
    // Data
    singleOf(::BibleVersionRepositoryImpl).bind<BibleVersionRepository>()

    // Domain
    factoryOf(::GetBibleVersionsUseCase)
    factoryOf(::SupabaseBookAbbreviationMapper)
    singleOf(::DownloadBibleUseCase)
    singleOf(::BibleVersionDownloaderFacadeImpl).bind<BibleVersionDownloaderFacade>()

    // Presentation
    viewModelOf(::BibleVersionViewModel)
}
