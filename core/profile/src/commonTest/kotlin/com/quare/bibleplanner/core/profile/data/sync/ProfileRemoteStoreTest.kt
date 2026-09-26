package com.quare.bibleplanner.core.profile.data.sync

import com.quare.bibleplanner.core.profile.data.dto.ProfileDto
import com.quare.bibleplanner.core.profile.data.dto.ProfileRowDto
import com.quare.bibleplanner.core.profile.data.mapper.ProfileMapper
import com.quare.bibleplanner.core.profile.fake.FakeRealtime
import com.quare.bibleplanner.core.profile.fake.RecordingSupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ProfileRemoteStoreTest {
    private val commitTimestamp = Instant.parse("2026-07-11T10:00:00Z")
    private val serializer = KotlinXSerializer(Json)
    private val row = ProfileRowDto(
        id = USER_ID,
        displayName = "Remote Name",
        avatarUrl = "https://storage.example/avatar.jpg",
        updatedAt = UPDATED_AT,
    )
    private val rowAsDto = ProfileDto(
        id = USER_ID,
        displayName = "Remote Name",
        avatarUrl = "https://storage.example/avatar.jpg",
        updatedAt = UPDATED_AT,
        isDisplayNameDirty = false,
        isAvatarDirty = false,
    )
    private lateinit var remoteStore: ProfileRemoteStore
    private lateinit var supabase: RecordingSupabaseClient
    private lateinit var realtime: FakeRealtime

    @Test
    fun `sends only the dirty display name`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.upsert(
            listOf(
                dto(
                    displayName = null,
                    isDisplayNameDirty = true,
                    isAvatarDirty = false,
                ),
            ),
        )

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = HttpMethod.Post,
            actual = request.method,
        )
        assertEquals(
            expected = "id",
            actual = request.query["on_conflict"],
        )
        assertEquals(
            expected = JsonArray(
                listOf(
                    JsonObject(
                        mapOf(
                            "id" to JsonPrimitive(USER_ID),
                            "updated_at" to JsonPrimitive(UPDATED_AT),
                            "display_name" to JsonNull,
                        ),
                    ),
                ),
            ),
            actual = Json.parseToJsonElement(request.body),
        )
    }

    @Test
    fun `sends only the dirty avatar url`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.upsert(
            listOf(
                dto(
                    displayName = "Name",
                    isDisplayNameDirty = false,
                    isAvatarDirty = true,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = JsonArray(
                listOf(
                    JsonObject(
                        mapOf(
                            "id" to JsonPrimitive(USER_ID),
                            "updated_at" to JsonPrimitive(UPDATED_AT),
                            "avatar_url" to JsonPrimitive(AVATAR_URL),
                        ),
                    ),
                ),
            ),
            actual = Json.parseToJsonElement(
                supabase.requests
                    .single()
                    .body,
            ),
        )
    }

    @Test
    fun `sends nothing for a profile with no dirty field`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.upsert(
            listOf(
                dto(
                    displayName = "Name",
                    isDisplayNameDirty = false,
                    isAvatarDirty = false,
                ),
            ),
        )

        // Then
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `fetches the profile row of the user`() = runTest {
        // Given
        prepareScenario(responseBody = Json.encodeToString(listOf(row)))

        // When
        val profiles = remoteStore.fetch(USER_ID)

        // Then
        assertEquals(
            expected = listOf(rowAsDto),
            actual = profiles,
        )
        assertEquals(
            expected = "eq.$USER_ID",
            actual = supabase.requests
                .single()
                .query["id"],
        )
    }

    @Test
    fun `emits inserted and updated rows and ignores deletions`() = runTest {
        // Given
        val record = Json.encodeToJsonElement(row).jsonObject
        prepareScenario(
            actions = listOf(
                PostgresAction.Insert(
                    record = record,
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
                PostgresAction.Delete(
                    oldRecord = record,
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
                PostgresAction.Update(
                    record = record,
                    oldRecord = record,
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
            ),
        )

        // When
        val emitted = remoteStore.observeRemote(USER_ID).toList()

        // Then
        assertEquals(
            expected = listOf(rowAsDto, rowAsDto),
            actual = emitted,
        )
        assertEquals(
            expected = listOf("user_data_$USER_ID"),
            actual = realtime.removedChannelIds,
        )
    }

    private fun dto(
        displayName: String?,
        isDisplayNameDirty: Boolean,
        isAvatarDirty: Boolean,
    ): ProfileDto = ProfileDto(
        id = USER_ID,
        displayName = displayName,
        avatarUrl = AVATAR_URL,
        updatedAt = UPDATED_AT,
        isDisplayNameDirty = isDisplayNameDirty,
        isAvatarDirty = isAvatarDirty,
    )

    private fun prepareScenario(
        responseBody: String = "[]",
        actions: List<PostgresAction> = emptyList(),
    ) {
        realtime = FakeRealtime(actions = flowOf(*actions.toTypedArray()))
        supabase = RecordingSupabaseClient(
            responseBody = responseBody,
            realtime = realtime,
        )
        remoteStore = ProfileRemoteStore(
            supabaseClient = supabase.client,
            profileMapper = ProfileMapper(),
        )
    }

    private companion object {
        const val USER_ID = "user-id"
        const val UPDATED_AT = "2026-07-11T10:00:00Z"
        const val AVATAR_URL = "https://storage.example/new.jpg"
    }
}
