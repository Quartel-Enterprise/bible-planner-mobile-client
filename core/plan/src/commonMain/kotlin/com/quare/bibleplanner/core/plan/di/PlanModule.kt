package com.quare.bibleplanner.core.plan.di

import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.ChaptersRangeMapper
import com.quare.bibleplanner.core.plan.data.mapper.DayMetaMapper
import com.quare.bibleplanner.core.plan.data.mapper.ReadingPlanPreferenceMapper
import com.quare.bibleplanner.core.plan.data.mapper.ReadingPlanPreferenceMapperImpl
import com.quare.bibleplanner.core.plan.data.mapper.UserPreferenceMapper
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.data.repository.PlanRepositoryImpl
import com.quare.bibleplanner.core.plan.data.sync.DayMetaLocalStore
import com.quare.bibleplanner.core.plan.data.sync.DayMetaRemoteStore
import com.quare.bibleplanner.core.plan.data.sync.SyncedPreferenceLocalStore
import com.quare.bibleplanner.core.plan.data.sync.UserPreferencesRemoteStore
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.DeleteDayNotesUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.EnsureDefaultPlanStartDateUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetDaysWithNotesCountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.MigratePlanPreferencesToSyncStoreUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.TrackReadingCompletionEventsUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayNotesUseCase
import com.quare.bibleplanner.core.sync.data.OfflineFirstSynchronizer
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val planModule = module {
    // Data sources
    singleOf(::PlanLocalDataSource)

    // Mappers
    factoryOf(::WeekPlanDtoToModelMapper)
    factoryOf(::ChaptersRangeMapper)
    factoryOf(::UserPreferenceMapper)
    singleOf(::ReadingPlanPreferenceMapperImpl).bind<ReadingPlanPreferenceMapper>()

    // Repository
    singleOf(::PlanRepositoryImpl).bind<PlanRepository>()

    // Sync
    factoryOf(::SyncedPreferenceLocalStore)
    factoryOf(::UserPreferencesRemoteStore)
    single<Synchronizer>(named("preferencesSync")) {
        OfflineFirstSynchronizer(
            localStore = get<SyncedPreferenceLocalStore>(),
            remoteStore = get<UserPreferencesRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            logTag = "PreferencesSync",
        )
    }
    factoryOf(::DayMetaMapper)
    factoryOf(::DayMetaLocalStore)
    factoryOf(::DayMetaRemoteStore)
    single<Synchronizer>(named("dayMetaSync")) {
        OfflineFirstSynchronizer(
            localStore = get<DayMetaLocalStore>(),
            remoteStore = get<DayMetaRemoteStore>(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
            logTag = "DayMetaSync",
        )
    }

    // Use cases
    factoryOf(::GetPlannedReadDateForDayUseCase)
    factoryOf(::GetPlansByWeekUseCase)
    factoryOf(::SetPlanStartTimeUseCase)
    factoryOf(::MigratePlanPreferencesToSyncStoreUseCase)
    factoryOf(::EnsureDefaultPlanStartDateUseCase)
    factoryOf(::UpdateDayNotesUseCase)
    factoryOf(::DeleteDayNotesUseCase)
    factoryOf(::GetDaysWithNotesCountUseCase)
    factoryOf(::GetMaxFreeNotesAmountUseCase)
    factoryOf(::GetPlanStartDateFlowUseCase)
    factoryOf(::TrackReadingCompletionEventsUseCase)
}
