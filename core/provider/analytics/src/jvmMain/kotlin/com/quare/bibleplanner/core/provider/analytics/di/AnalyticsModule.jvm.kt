package com.quare.bibleplanner.core.provider.analytics.di

import com.quare.bibleplanner.core.provider.analytics.ClientIdProvider
import com.quare.bibleplanner.core.provider.analytics.DesktopAnalyticsService
import com.quare.bibleplanner.core.provider.analytics.MeasurementProtocolClient
import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

private const val REQUEST_TIMEOUT_MILLIS = 10_000L

internal actual val platformAnalyticsModule: Module = module {
    singleOf(::ClientIdProvider)
    single {
        MeasurementProtocolClient(
            httpClient = createHttpClient(),
            clientIdProvider = get(),
        )
    }
    singleOf(::DesktopAnalyticsService).bind<AnalyticsService>()
}

private fun createHttpClient(): HttpClient = HttpClient(CIO) {
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}
