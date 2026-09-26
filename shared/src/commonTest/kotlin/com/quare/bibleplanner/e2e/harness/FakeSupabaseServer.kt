package com.quare.bibleplanner.e2e.harness

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val LIST_BIBLE_FOLDERS_PATH = "/storage/v1/object/list/content"
private const val PUBLIC_BIBLE_PATH = "/storage/v1/object/public/content/bible/"
private const val JSON_EXTENSION = ".json"
private const val CONTENT_VERSION = "1"
private const val BIBLE_CHAPTERS = 1189
private const val MAX_VERSES_PER_CHAPTER = 176
private const val UNAUTHORIZED_BODY = """{"message":"Signed out in the end-to-end tests"}"""
private const val NOT_FOUND_BODY = """{"message":"Not found"}"""
private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

// Stands in for the Supabase project. Storage serves the Bible versions of FakeBibles, the only
// content the app downloads before it can show a chapter, and every other endpoint answers as a
// signed-out user would see it: nothing to sync, nothing to show.
internal fun fakeSupabaseEngine(): MockEngine = MockEngine(MockRequestHandleScope::respondAsSupabase)

private fun MockRequestHandleScope.respondAsSupabase(request: HttpRequestData): HttpResponseData {
    val path = request.url.encodedPath
    return when {
        request.method == HttpMethod.Post && path.endsWith(LIST_BIBLE_FOLDERS_PATH) -> json(versionFolders().toString())

        path.contains(PUBLIC_BIBLE_PATH) -> serveBibleFile(path.substringAfter(PUBLIC_BIBLE_PATH))

        else -> respond(
            content = UNAUTHORIZED_BODY,
            status = HttpStatusCode.Unauthorized,
            headers = jsonHeaders,
        )
    }
}

private fun MockRequestHandleScope.serveBibleFile(bibleFilePath: String): HttpResponseData {
    val segments = bibleFilePath.split('/')
    val version = FakeBibles.versions.firstOrNull { version -> version.id == segments.first() }
        ?: return notFound()
    return when (segments.size) {
        2 -> json(version.metadata().toString())
        3 -> json(chapter(version = version, bookDirectory = segments[1], chapterFile = segments[2]))
        else -> notFound()
    }
}

private fun versionFolders(): JsonArray = buildJsonArray {
    FakeBibles.versions.forEach { version ->
        add(
            buildJsonObject {
                put("name", version.id)
                put("id", JsonNull)
                put("updated_at", JsonNull)
                put("created_at", JsonNull)
                put("last_accessed_at", JsonNull)
                put("metadata", JsonNull)
            },
        )
    }
}

private fun FakeBibleVersion.metadata(): JsonObject = buildJsonObject {
    put("id", id)
    put("name", name)
    put("version", CONTENT_VERSION)
    put("language", language)
    put("country", country)
    put("chapters", BIBLE_CHAPTERS)
    put("size", JsonNull)
}

private fun chapter(
    version: FakeBibleVersion,
    bookDirectory: String,
    chapterFile: String,
): String {
    val chapterNumber = chapterFile.removeSuffix(JSON_EXTENSION).toInt()
    return buildJsonObject {
        put("chapter", chapterNumber)
        put(
            "verses",
            buildJsonArray {
                (1..MAX_VERSES_PER_CHAPTER).forEach { verseNumber ->
                    add(
                        buildJsonObject {
                            put("number", verseNumber)
                            put(
                                "text",
                                FakeBibles.verseText(
                                    version = version,
                                    bookDirectory = bookDirectory,
                                    chapter = chapterNumber,
                                    verse = verseNumber,
                                ),
                            )
                            put("heading", JsonNull)
                        },
                    )
                }
            },
        )
    }.toString()
}

private fun MockRequestHandleScope.json(content: String): HttpResponseData = respond(
    content = content,
    status = HttpStatusCode.OK,
    headers = jsonHeaders,
)

private fun MockRequestHandleScope.notFound(): HttpResponseData = respond(
    content = NOT_FOUND_BODY,
    status = HttpStatusCode.NotFound,
    headers = jsonHeaders,
)
