package com.quare.bibleplanner.core.profile.data.sync

import com.quare.bibleplanner.core.profile.fake.FakeRealtime
import com.quare.bibleplanner.core.profile.fake.RecordingSupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarRemoteStoreTest {
    private lateinit var avatarRemoteStore: AvatarRemoteStore
    private lateinit var supabase: RecordingSupabaseClient

    @BeforeTest
    fun setUp() {
        supabase = RecordingSupabaseClient(
            responseBody = UPLOAD_RESPONSE,
            realtime = FakeRealtime(actions = emptyFlow()),
        )
        avatarRemoteStore = AvatarRemoteStore(supabase.client.storage.from(BUCKET))
    }

    @Test
    fun `uploads the avatar over the previous one and returns its public url`() = runTest {
        // When
        val url = avatarRemoteStore.upload(
            userId = USER_ID,
            bytes = byteArrayOf(1, 2, 3),
        )

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = HttpMethod.Post,
            actual = request.method,
        )
        assertEquals(
            expected = "/storage/v1/object/avatars/$USER_ID/avatar.jpg",
            actual = request.path,
        )
        assertEquals(
            expected = "true",
            actual = request.headers["x-upsert"],
        )
        assertEquals(
            expected = "https://project.supabase.co/storage/v1/object/public/avatars/$USER_ID/avatar.jpg",
            actual = url,
        )
    }

    @Test
    fun `deletes the avatar of the user`() = runTest {
        // When
        avatarRemoteStore.delete(USER_ID)

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = HttpMethod.Delete,
            actual = request.method,
        )
        assertEquals(
            expected = JsonObject(mapOf("prefixes" to JsonArray(listOf(JsonPrimitive("$USER_ID/avatar.jpg"))))),
            actual = Json.parseToJsonElement(request.body),
        )
    }

    private companion object {
        const val USER_ID = "user-id"
        const val BUCKET = "avatars"
        const val UPLOAD_RESPONSE = """{"Id":"file-1","Key":"avatars/user-id/avatar.jpg"}"""
    }
}
