package com.quare.bibleplanner.core.user.data.mapper

import com.quare.bibleplanner.core.user.domain.model.UserModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SessionUserMapperTest {
    private val lastSignInAt = Instant.parse("2026-07-11T10:00:00Z")
    private val createdAt = Instant.parse("2025-01-01T00:00:00Z")
    private lateinit var mapper: SessionUserMapper

    @BeforeTest
    fun setUp() {
        mapper = SessionUserMapper()
    }

    @Test
    fun `GIVEN a Google user WHEN mapping THEN reads the name photo and provider from the metadata`() {
        // Given
        val sessionUser = sessionUser(
            userMetadata = metadata(
                "name" to "Ana",
                "avatar_url" to PHOTO_URL,
            ),
            appMetadata = metadata("provider" to "google"),
        )

        // When
        val user = mapper.map(sessionUser)

        // Then
        assertEquals(
            expected = UserModel(
                id = USER_ID,
                name = "Ana",
                email = EMAIL,
                photo = PHOTO_URL,
                provider = "google",
                lastSignInAt = lastSignInAt,
                createdAt = createdAt,
            ),
            actual = user,
        )
    }

    @Test
    fun `GIVEN an Apple user without a name WHEN mapping THEN falls back to the full name and no photo`() {
        // Given
        val sessionUser = sessionUser(
            userMetadata = metadata("full_name" to "Ana Souza"),
            appMetadata = null,
        )

        // When
        val user = mapper.map(sessionUser)

        // Then
        assertEquals(
            expected = "Ana Souza",
            actual = user?.name,
        )
        assertNull(user?.photo)
        assertNull(user?.provider)
    }

    @Test
    fun `GIVEN a user without metadata WHEN mapping THEN maps to no user`() {
        // Given
        val sessionUser = sessionUser(
            userMetadata = null,
            appMetadata = null,
        )

        // When
        val user = mapper.map(sessionUser)

        // Then
        assertNull(user)
    }

    @Test
    fun `GIVEN a user without an email WHEN mapping THEN maps to no user`() {
        // Given
        val sessionUser = sessionUser(
            userMetadata = metadata("name" to "Ana"),
            appMetadata = null,
            email = null,
        )

        // When
        val user = mapper.map(sessionUser)

        // Then
        assertNull(user)
    }

    private fun metadata(vararg entries: Pair<String, String>): JsonObject =
        JsonObject(entries.associate { (key, value) -> key to JsonPrimitive(value) })

    private fun sessionUser(
        userMetadata: JsonObject?,
        appMetadata: JsonObject?,
        email: String? = EMAIL,
    ): UserInfo = UserInfo(
        aud = "authenticated",
        id = USER_ID,
        email = email,
        userMetadata = userMetadata,
        appMetadata = appMetadata,
        lastSignInAt = lastSignInAt,
        createdAt = createdAt,
    )

    private companion object {
        const val USER_ID = "user-1"
        const val EMAIL = "ana@example.com"
        const val PHOTO_URL = "https://provider.example/ana.jpg"
    }
}
