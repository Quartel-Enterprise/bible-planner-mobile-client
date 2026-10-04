package com.quare.bibleplanner.tools.agentcli.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class JsonPath(
    path: String,
) {
    private val pathToken = Regex("""([^.\[\]]+)|\[(\d+)]""")
    private val segments: List<Segment> = pathToken
        .findAll(path)
        .map { match ->
            match.groups[2]?.let { index -> Segment.Index(index.value.toInt()) } ?: Segment.Key(match.value)
        }.toList()

    fun select(root: JsonElement): JsonElement {
        var current = root
        segments.forEachIndexed { index, segment ->
            val at = segments.take(index + 1).joinToString(separator = "")
            current = when (segment) {
                is Segment.Key -> requireNotNull((current as? JsonObject)?.get(segment.name)) {
                    "no \"${segment.name}\" at ${at.removePrefix(".")}; keys: ${(current as? JsonObject)?.keys}"
                }

                is Segment.Index -> requireNotNull((current as? JsonArray)?.getOrNull(segment.position)) {
                    "no item ${segment.position} at ${at.removePrefix(".")}"
                }
            }
        }
        return current
    }

    sealed interface Segment {
        data class Key(
            val name: String,
        ) : Segment {
            override fun toString(): String = ".$name"
        }

        data class Index(
            val position: Int,
        ) : Segment {
            override fun toString(): String = "[$position]"
        }
    }
}
