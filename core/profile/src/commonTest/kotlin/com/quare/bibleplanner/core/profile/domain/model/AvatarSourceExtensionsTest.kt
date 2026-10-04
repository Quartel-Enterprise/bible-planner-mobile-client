package com.quare.bibleplanner.core.profile.domain.model

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AvatarSourceExtensionsTest {
    @Test
    fun `GIVEN a remote avatar WHEN reading its photo THEN exposes the url and no bytes`() {
        // Given
        val avatar = AvatarSource.Remote("https://storage.example/avatar.jpg")

        // When
        val url = avatar.photoUrl
        val bytes = avatar.photoBytes

        // Then
        assertEquals(
            expected = "https://storage.example/avatar.jpg",
            actual = url,
        )
        assertNull(bytes)
    }

    @Test
    fun `GIVEN a pending avatar WHEN reading its photo THEN exposes the bytes and no url`() {
        // Given
        val avatar = AvatarSource.Pending(byteArrayOf(1, 2))

        // When
        val url = avatar.photoUrl
        val bytes = avatar.photoBytes

        // Then
        assertNull(url)
        assertContentEquals(
            expected = byteArrayOf(1, 2),
            actual = bytes,
        )
    }

    @Test
    fun `GIVEN two pending avatars with the same bytes WHEN comparing them THEN they are equal`() {
        // Given
        val first = AvatarSource.Pending(byteArrayOf(1, 2))
        val second = AvatarSource.Pending(byteArrayOf(1, 2))

        // When
        val firstHashCode = first.hashCode()
        val secondHashCode = second.hashCode()

        // Then
        assertEquals(
            expected = first,
            actual = second,
        )
        assertEquals(
            expected = firstHashCode,
            actual = secondHashCode,
        )
    }
}
