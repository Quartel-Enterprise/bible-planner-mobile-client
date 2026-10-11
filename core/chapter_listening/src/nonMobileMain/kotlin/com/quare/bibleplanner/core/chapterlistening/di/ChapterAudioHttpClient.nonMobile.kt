package com.quare.bibleplanner.core.chapterlistening.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

// Why: listening is off on these platforms, so the client is never used and takes whichever engine is there.
internal actual fun createChapterAudioHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(configure)
