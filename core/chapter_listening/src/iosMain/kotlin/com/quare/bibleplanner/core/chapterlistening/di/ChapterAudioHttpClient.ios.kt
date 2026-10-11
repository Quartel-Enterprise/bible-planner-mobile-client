package com.quare.bibleplanner.core.chapterlistening.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

internal actual fun createChapterAudioHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Darwin, configure)
