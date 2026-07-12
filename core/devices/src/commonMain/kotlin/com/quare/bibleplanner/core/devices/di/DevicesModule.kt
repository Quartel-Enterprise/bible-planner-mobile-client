package com.quare.bibleplanner.core.devices.di

import com.quare.bibleplanner.core.devices.data.DeviceIdProvider
import com.quare.bibleplanner.core.devices.data.DeviceInfoProvider
import com.quare.bibleplanner.core.devices.data.UserDevicesRemoteStore
import com.quare.bibleplanner.core.devices.data.local.UserDeviceLocalStore
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceDtoToEntityMapper
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceEntityToDomainMapper
import com.quare.bibleplanner.core.devices.data.repository.DevicesRepositoryImpl
import com.quare.bibleplanner.core.devices.data.sync.DevicesSynchronizer
import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveCurrentDeviceRevoked
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDeviceRegistration
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDevices
import com.quare.bibleplanner.core.devices.domain.usecase.RenameDevice
import com.quare.bibleplanner.core.devices.domain.usecase.SignOutDevice
import com.quare.bibleplanner.core.devices.domain.usecase.UnregisterCurrentDevice
import com.quare.bibleplanner.core.devices.domain.usecase.impl.ObserveCurrentDeviceRevokedUseCase
import com.quare.bibleplanner.core.devices.domain.usecase.impl.ObserveDeviceRegistrationUseCase
import com.quare.bibleplanner.core.devices.domain.usecase.impl.ObserveDevicesUseCase
import com.quare.bibleplanner.core.devices.domain.usecase.impl.RenameDeviceUseCase
import com.quare.bibleplanner.core.devices.domain.usecase.impl.SignOutDeviceUseCase
import com.quare.bibleplanner.core.devices.domain.usecase.impl.UnregisterCurrentDeviceUseCase
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val devicesModule = module {
    // Data
    singleOf(::UserDevicesRemoteStore)
    factoryOf(::UserDeviceDtoToEntityMapper)
    factoryOf(::UserDeviceEntityToDomainMapper)
    factoryOf(::DeviceIdProvider)
    factoryOf(::DeviceInfoProvider)
    factoryOf(::UserDeviceLocalStore)
    singleOf(::DevicesRepositoryImpl).bind<DevicesRepository>()
    single<Synchronizer>(named("devicesSync")) {
        DevicesSynchronizer(
            localStore = get(),
            remoteStore = get(),
            networkConnectivityObserver = get(),
            getAuthenticatedUserId = get(),
        )
    }

    // Domain
    factoryOf(::ObserveDevicesUseCase).bind<ObserveDevices>()
    factoryOf(::RenameDeviceUseCase).bind<RenameDevice>()
    factoryOf(::SignOutDeviceUseCase).bind<SignOutDevice>()
    factoryOf(::ObserveDeviceRegistrationUseCase).bind<ObserveDeviceRegistration>()
    factoryOf(::ObserveCurrentDeviceRevokedUseCase).bind<ObserveCurrentDeviceRevoked>()
    factoryOf(::UnregisterCurrentDeviceUseCase).bind<UnregisterCurrentDevice>()
}
