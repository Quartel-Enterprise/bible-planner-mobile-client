package com.quare.bibleplanner.core.network.data.di

import com.quare.bibleplanner.core.network.data.handler.RequestHandler
import com.quare.bibleplanner.core.network.data.utils.getHttpClient
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    singleOf(::RequestHandler)
    single<HttpClient> {
        getHttpClient()
    }
}
