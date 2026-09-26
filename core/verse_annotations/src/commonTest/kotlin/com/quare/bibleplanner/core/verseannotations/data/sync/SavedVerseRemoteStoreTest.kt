package com.quare.bibleplanner.core.verseannotations.data.sync

import com.quare.bibleplanner.core.verseannotations.data.dto.SavedVerseDto
import com.quare.bibleplanner.core.verseannotations.fake.FakeRealtime
import com.quare.bibleplanner.core.verseannotations.fake.RecordingSupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

internal class SavedVerseRemoteStoreTest {
    private val commitTimestamp = Instant.parse("2026-07-11T10:00:00Z")
    private val serializer = KotlinXSerializer(Json)
    private val dto = SavedVerseDto(
        userId = USER_ID,
        bibleVersionId = "ACF",
        bookId = "JHN",
        chapterNumber = 3,
        verseNumber = 16,
        isSaved = true,
        updatedAt = "2026-07-11T10:00:00Z",
    )
    private lateinit var remoteStore: SavedVerseRemoteStore
    private lateinit var supabase: RecordingSupabaseClient
    private lateinit var realtime: FakeRealtime

    @Test
    fun `upserts the rows on the saved verses table keyed by the verse coordinates`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.upsert(listOf(dto))

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = HttpMethod.Post,
            actual = request.method,
        )
        assertEquals(
            expected = "/rest/v1/saved_verses",
            actual = request.path,
        )
        assertEquals(
            expected = "user_id,bible_version_id,book_id,chapter_number,verse_number",
            actual = request.query["on_conflict"],
        )
        assertEquals(
            expected = listOf(dto),
            actual = Json.decodeFromString<List<SavedVerseDto>>(request.body),
        )
    }

    @Test
    fun `fetches the rows of the user`() = runTest {
        // Given
        prepareScenario(responseBody = Json.encodeToString(listOf(dto)))

        // When
        val rows = remoteStore.fetch(USER_ID)

        // Then
        assertEquals(
            expected = listOf(dto),
            actual = rows,
        )
        assertEquals(
            expected = "eq.$USER_ID",
            actual = supabase.requests.single().query["user_id"],
        )
    }

    @Test
    fun `emits inserted and updated rows and ignores deletions`() = runTest {
        // Given
        val updated = dto.copy(isSaved = false)
        prepareScenario(
            actions = listOf(
                insert(record = Json.encodeToJsonElement(dto).jsonObject),
                delete(),
                update(record = Json.encodeToJsonElement(updated).jsonObject),
            ),
        )

        // When
        val emitted = remoteStore.observeRemote(USER_ID).toList()

        // Then
        assertEquals(
            expected = listOf(dto, updated),
            actual = emitted,
        )
    }

    @Test
    fun `subscribes to a channel of the user and removes it once the stream ends`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.observeRemote(USER_ID).toList()

        // Then
        assertEquals(
            expected = listOf("saved_verses_$USER_ID"),
            actual = realtime.subscribedChannelIds,
        )
        assertEquals(
            expected = listOf("saved_verses_$USER_ID"),
            actual = realtime.removedChannelIds,
        )
    }

    private fun insert(record: JsonObject): PostgresAction = PostgresAction.Insert(
        record = record,
        columns = emptyList(),
        commitTimestamp = commitTimestamp,
        serializer = serializer,
    )

    private fun update(record: JsonObject): PostgresAction = PostgresAction.Update(
        record = record,
        oldRecord = JsonObject(emptyMap()),
        columns = emptyList(),
        commitTimestamp = commitTimestamp,
        serializer = serializer,
    )

    private fun delete(): PostgresAction = PostgresAction.Delete(
        oldRecord = JsonObject(emptyMap()),
        columns = emptyList(),
        commitTimestamp = commitTimestamp,
        serializer = serializer,
    )

    private fun prepareScenario(
        responseBody: String = "[]",
        actions: List<PostgresAction> = emptyList(),
    ) {
        supabase = RecordingSupabaseClient(responseBody = responseBody)
        realtime = FakeRealtime(actions = flowOf(*actions.toTypedArray()))
        remoteStore = SavedVerseRemoteStore(
            supabaseClient = supabase.client,
            realtime = realtime,
        )
    }

    private companion object {
        const val USER_ID = "user-1"
    }
}
