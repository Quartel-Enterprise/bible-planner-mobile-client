package com.quare.bibleplanner.core.chapterlistening.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createChapterAudioHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp, configure)
