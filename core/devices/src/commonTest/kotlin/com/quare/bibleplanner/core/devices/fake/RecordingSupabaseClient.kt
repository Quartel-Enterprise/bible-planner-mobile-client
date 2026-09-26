package com.quare.bibleplanner.core.devices.fake

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers

internal class RecordingSupabaseClient(
    responseBody: String,
) {
    val requests = mutableListOf<RecordedRequest>()
    var responseStatus: HttpStatusCode = HttpStatusCode.OK

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://project.supabase.co",
        supabaseKey = "anon-key",
    ) {
        httpEngine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler { request ->
                    requests += RecordedRequest(
                        method = request.method,
                        path = request.url.encodedPath,
                        query = request.url.parameters.entries().associate { (name, values) ->
                            name to
                                values.joinToString(",")
                        },
                        body = request.body.toByteArray().decodeToString(),
                    )
                    respond(
                        content = responseBody,
                        status = responseStatus,
                        headers = headersOf(
                            name = HttpHeaders.ContentType,
                            value = "application/json",
                        ),
                    )
                }
            },
        )
        install(Postgrest)
        install(Functions)
    }

    val functions: Functions
        get() = client.functions
}
