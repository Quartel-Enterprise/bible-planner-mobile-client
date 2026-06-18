package com.quare.bibleplanner.core.date.di

import com.quare.bibleplanner.core.date.AndroidTrustedTimestampProvider
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.DeviceClockTimestampProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val trustedTimeModule: Module = module {
    singleOf(::DeviceClockTimestampProvider)
    singleOf(::AndroidTrustedTimestampProvider).bind<CurrentTimestampProvider>()
}
