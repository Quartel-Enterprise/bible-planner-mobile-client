package com.quare.bibleplanner.core.profile.domain.model

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AvatarSourceExtensionsTest {
    @Test
    fun `exposes the url of a remote avatar and no bytes`() {
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
    fun `exposes the bytes of a pending avatar and no url`() {
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
    fun `pending avatars are equal when they hold the same bytes`() {
        // When
        val first = AvatarSource.Pending(byteArrayOf(1, 2))
        val second = AvatarSource.Pending(byteArrayOf(1, 2))

        // Then
        assertEquals(
            expected = first,
            actual = second,
        )
        assertEquals(
            expected = first.hashCode(),
            actual = second.hashCode(),
        )
    }
}
