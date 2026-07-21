package com.quare.bibleplanner.core.profile.di

import com.quare.bibleplanner.core.profile.data.mapper.ProfileMapper
import com.quare.bibleplanner.core.profile.data.mapper.UserProfileMapper
import com.quare.bibleplanner.core.profile.data.repository.ProfileRepositoryImpl
import com.quare.bibleplanner.core.profile.data.sync.AvatarRemoteStore
import com.quare.bibleplanner.core.profile.data.sync.ProfileLocalStore
import com.quare.bibleplanner.core.profile.data.sync.ProfileRemoteStore
import com.quare.bibleplanner.core.profile.data.sync.ProfileSynchronizer
import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.profile.domain.usecase.impl.ObserveUserProfileUseCase
import com.quare.bibleplanner.core.provider.supabase.AVATARS_BUCKET
import com.quare.bibleplanner.core.sync.data.OfflineFirstSynchronizer
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val profileModule = module {
    // Data
    factoryOf(::ProfileMapper)
    factoryOf(::UserProfileMapper)
    singleOf(::ProfileLocalStore)
    single { ProfileRemoteStore(supabaseClient = get(), profileMapper = get()) }
    single {
        AvatarRemoteStore(bucketApi = get(named(AVATARS_BUCKET)))
    }

    // Repository
    singleOf(::ProfileRepositoryImpl).bind<ProfileRepository>()

    // Sync
    single<Synchronizer>(named("profileSync")) {
        ProfileSynchronizer(
            delegate = OfflineFirstSynchronizer(
                localStore = get<ProfileLocalStore>(),
                remoteStore = get<ProfileRemoteStore>(),
                networkConnectivityObserver = get(),
                getAuthenticatedUserId = get(),
                logTag = "ProfileSync",
            ),
            profileDao = get(),
            avatarRemoteStore = get(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
        )
    }

    // Domain
    factoryOf(::ObserveUserProfileUseCase).bind<ObserveUserProfile>()
}
