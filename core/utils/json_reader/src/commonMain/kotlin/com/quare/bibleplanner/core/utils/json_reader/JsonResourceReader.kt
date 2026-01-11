package com.quare.bibleplanner.core.utils.json_reader

import kotlinx.serialization.json.Json

class JsonResourceReader {
    val json: Json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> read(
        path: String,
        readBytes: (path: String) -> ByteArray,
    ): T {
        val bytes = readBytes(path)
        val content = bytes.decodeToString()
        return json.decodeFromString(content)
    }
}
