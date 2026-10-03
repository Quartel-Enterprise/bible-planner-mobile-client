package com.quare.bibleplanner.core.chapterstudy.di

import com.quare.bibleplanner.core.chapterstudy.data.datasource.ChapterStudyLocalDataSource
import com.quare.bibleplanner.core.chapterstudy.data.datasource.ChapterStudyRemoteDataSource
import com.quare.bibleplanner.core.chapterstudy.data.datasource.ChapterStudyRemoteDataSourceImpl
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyCacheKeyFactory
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyEntityMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyPhaseMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyRequestMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyStatusMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.WireNameBookIdMapper
import com.quare.bibleplanner.core.chapterstudy.data.repository.ChapterStudyRepositoryImpl
import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinatorImpl
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.domain.store.PendingVerseFocusStore
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.FindCachedChapterStudy
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GenerateChapterStudy
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyAccess
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyQuota
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.RefreshChapterStudyCache
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.ChapterStudyScopeResolver
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.ClearChapterStudyLocalDataUseCase
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.FindCachedChapterStudyUseCase
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.GenerateChapterStudyUseCase
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.GetChapterStudyAccessUseCase
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.GetChapterStudyQuotaUseCase
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.RefreshChapterStudyCacheUseCase
import com.quare.bibleplanner.core.clear.domain.ClearChapterStudyLocalData
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chapterStudyModule = module {
    factoryOf(::ChapterStudyRequestMapper)
    factoryOf(::WireNameBookIdMapper)
    factoryOf(::ChapterStudyCacheKeyFactory)
    factoryOf(::ChapterStudyEntityMapper)
    factoryOf(::ChapterStudyStatusMapper)
    factoryOf(::ChapterStudyPhaseMapper)
    factoryOf(::ChapterStudyScopeResolver)

    singleOf(::ChapterStudyRemoteDataSourceImpl).bind<ChapterStudyRemoteDataSource>()
    singleOf(::ChapterStudyLocalDataSource)
    singleOf(::ChapterStudyRepositoryImpl).bind<ChapterStudyRepository>()
    singleOf(::ChapterStudyGenerationCoordinatorImpl).bind<ChapterStudyGenerationCoordinator>()
    singleOf(::PendingVerseFocusStore)

    factoryOf(::GenerateChapterStudyUseCase).bind<GenerateChapterStudy>()
    factoryOf(::FindCachedChapterStudyUseCase).bind<FindCachedChapterStudy>()
    factoryOf(::GetChapterStudyAccessUseCase).bind<GetChapterStudyAccess>()
    factoryOf(::GetChapterStudyQuotaUseCase).bind<GetChapterStudyQuota>()
    factoryOf(::RefreshChapterStudyCacheUseCase).bind<RefreshChapterStudyCache>()
    factoryOf(::ClearChapterStudyLocalDataUseCase).bind<ClearChapterStudyLocalData>()
}
