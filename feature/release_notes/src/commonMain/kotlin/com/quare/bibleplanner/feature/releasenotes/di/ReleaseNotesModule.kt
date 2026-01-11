package com.quare.bibleplanner.feature.releasenotes.di

import com.quare.bibleplanner.feature.releasenotes.data.mapper.GitHubReleaseDateMapper
import com.quare.bibleplanner.feature.releasenotes.data.repository.GitHubReleaseNotesRepository
import com.quare.bibleplanner.feature.releasenotes.domain.repository.ReleaseNotesRepository
import com.quare.bibleplanner.feature.releasenotes.domain.usecase.GetReleaseNotesUseCase
import com.quare.bibleplanner.feature.releasenotes.presentation.factory.ReleaseNotesUiStateFactory
import com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel.ReleaseNotesViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val releaseNotesModule = module {
    factoryOf(::GitHubReleaseDateMapper)
    factoryOf(::GitHubReleaseNotesRepository) { bind<ReleaseNotesRepository>() }
    factoryOf(::GetReleaseNotesUseCase)
    factory { ReleaseNotesUiStateFactory(get()) }
    viewModelOf(::ReleaseNotesViewModel)
}
