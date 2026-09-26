package com.quare.bibleplanner.feature.bibleversion.fake

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers

internal class StorageServer(
    filesByPath: Map<String, String>,
    failuresBeforeSuccessByPath: Map<String, Int> = emptyMap(),
) {
    val requestedPaths = mutableListOf<String>()

    val bucketApi: BucketApi = createSupabaseClient(
        supabaseUrl = "https://project.supabase.co",
        supabaseKey = "anon-key",
    ) {
        httpEngine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler { request ->
                    val path = request.url.encodedPath.removePrefix(PUBLIC_PREFIX)
                    val attempt = synchronized(requestedPaths) {
                        requestedPaths += path
                        requestedPaths.count { it == path }
                    }
                    val file = filesByPath[path]
                    val failures = failuresBeforeSuccessByPath[path] ?: 0
                    if (file == null || attempt <= failures) {
                        respondError(HttpStatusCode.InternalServerError)
                    } else {
                        respond(file)
                    }
                }
            },
        )
        install(Storage)
    }.storage.from(BUCKET)

    private companion object {
        const val BUCKET = "content"
        const val PUBLIC_PREFIX = "/storage/v1/object/public/$BUCKET/"
    }
}
