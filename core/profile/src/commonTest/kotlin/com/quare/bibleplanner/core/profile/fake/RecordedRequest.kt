package com.quare.bibleplanner.core.profile.fake

import io.ktor.http.HttpMethod

internal data class RecordedRequest(
    val method: HttpMethod,
    val path: String,
    val query: Map<String, String>,
    val headers: Map<String, String>,
    val body: String,
)
