package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.VersionDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

internal class CachedVersionsJsonMapper(
    private val json: Json,
) {
    fun map(cachedJson: String): List<VersionDto> = json
        .parseToJsonElement(cachedJson)
        .jsonArray
        .map { element ->
            val jsonObject = element.jsonObject
            VersionDto(
                id = jsonObject.getValue("id").jsonPrimitive.content,
                name = jsonObject.getValue("name").jsonPrimitive.content,
                version = jsonObject["version"]?.jsonPrimitive?.content.orEmpty(),
                language = jsonObject.getValue("language").jsonPrimitive.content,
                country = jsonObject.getValue("country").jsonPrimitive.content,
                chapters = jsonObject.getValue("chapters").jsonPrimitive.int,
                size = jsonObject["size"]?.jsonPrimitive?.long,
            )
        }
}
