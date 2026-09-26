package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class ProfileDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ProfileDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.profileDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN no profile WHEN setting the display name THEN creates a row pending only for the name`() = runTest {
        // When
        dao.setDisplayNameLocal(
            id = USER_ID,
            displayName = "Ana",
            updatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = profile(
                displayName = "Ana",
                updatedAt = 10L,
                displayNamePendingSync = true,
            ),
            actual = dao.observeProfile(USER_ID).first(),
        )
    }

    @Test
    fun `GIVEN a profile WHEN setting the display name THEN keeps its avatar`() = runTest {
        // Given
        dao.setAvatarLocal(
            id = USER_ID,
            avatarUrl = AVATAR_URL,
            pendingAvatarBytes = null,
            updatedAt = 10L,
        )

        // When
        dao.setDisplayNameLocal(
            id = USER_ID,
            displayName = "Ana",
            updatedAt = 20L,
        )

        // Then
        assertEquals(
            expected = profile(
                displayName = "Ana",
                avatarUrl = AVATAR_URL,
                updatedAt = 20L,
                displayNamePendingSync = true,
                avatarPendingSync = true,
            ),
            actual = dao.observeProfile(USER_ID).first(),
        )
    }

    @Test
    fun `GIVEN a picked photo WHEN it is uploaded THEN stores the url and drops the bytes`() = runTest {
        // Given
        dao.setAvatarLocal(
            id = USER_ID,
            avatarUrl = null,
            pendingAvatarBytes = byteArrayOf(1, 2, 3),
            updatedAt = 10L,
        )

        // When
        dao.onAvatarUploaded(
            id = USER_ID,
            avatarUrl = AVATAR_URL,
            pendingUpdatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = profile(
                avatarUrl = AVATAR_URL,
                updatedAt = 10L,
                avatarPendingSync = true,
            ),
            actual = dao.observeProfile(USER_ID).first(),
        )
    }

    @Test
    fun `GIVEN pending changes WHEN marking only the name synced THEN keeps the avatar pending`() = runTest {
        // Given
        dao.setAvatarLocal(
            id = USER_ID,
            avatarUrl = AVATAR_URL,
            pendingAvatarBytes = null,
            updatedAt = 10L,
        )
        dao.setDisplayNameLocal(
            id = USER_ID,
            displayName = "Ana",
            updatedAt = 10L,
        )

        // When
        dao.markSynced(
            id = USER_ID,
            syncedUpdatedAt = 10L,
            clearDisplayName = true,
            clearAvatar = false,
        )

        // Then
        val pending = dao.getPending().single()
        assertEquals(
            expected = false to true,
            actual = pending.displayNamePendingSync to pending.avatarPendingSync,
        )
        assertEquals(
            expected = listOf(pending),
            actual = dao.getPendingFlow().first(),
        )
    }

    @Test
    fun `GIVEN an unknown profile WHEN applying a remote one THEN inserts it as synced`() = runTest {
        // When
        dao.applyRemote(
            id = USER_ID,
            displayName = "Remote",
            avatarUrl = AVATAR_URL,
            remoteUpdatedAt = 30L,
        )

        // Then
        assertEquals(
            expected = profile(
                displayName = "Remote",
                avatarUrl = AVATAR_URL,
                updatedAt = 30L,
            ),
            actual = dao.observeProfile(USER_ID).first(),
        )
    }

    @Test
    fun `GIVEN an older synced profile WHEN applying a newer remote one THEN overwrites it`() = runTest {
        // Given
        dao.applyRemote(
            id = USER_ID,
            displayName = "Old",
            avatarUrl = null,
            remoteUpdatedAt = 10L,
        )

        // When
        dao.applyRemote(
            id = USER_ID,
            displayName = "New",
            avatarUrl = null,
            remoteUpdatedAt = 20L,
        )

        // Then
        assertEquals(
            expected = "New",
            actual = dao.observeProfile(USER_ID).first()?.displayName,
        )
    }

    @Test
    fun `GIVEN a pending local edit WHEN applying a remote one THEN keeps the local edit`() = runTest {
        // Given
        dao.setDisplayNameLocal(
            id = USER_ID,
            displayName = "Local",
            updatedAt = 10L,
        )

        // When
        dao.applyRemote(
            id = USER_ID,
            displayName = "Remote",
            avatarUrl = null,
            remoteUpdatedAt = 20L,
        )

        // Then
        assertEquals(
            expected = "Local",
            actual = dao.observeProfile(USER_ID).first()?.displayName,
        )
    }

    @Test
    fun `GIVEN profiles WHEN deleting all THEN none is left`() = runTest {
        // Given
        dao.setDisplayNameLocal(
            id = USER_ID,
            displayName = "Ana",
            updatedAt = 10L,
        )

        // When
        dao.deleteAll()

        // Then
        assertTrue(dao.getPending().isEmpty())
    }

    @Test
    fun `GIVEN two profiles with the same avatar bytes WHEN comparing THEN they are equal`() {
        // Given
        val first = profile(pendingAvatarBytes = byteArrayOf(1, 2))
        val second = profile(pendingAvatarBytes = byteArrayOf(1, 2))

        // When
        val areEqual = first == second

        // Then
        assertTrue(areEqual)
        assertEquals(
            expected = first.hashCode(),
            actual = second.hashCode(),
        )
        assertNotEquals(
            illegal = first,
            actual = profile(pendingAvatarBytes = byteArrayOf(3)),
        )
    }

    private fun profile(
        displayName: String? = null,
        avatarUrl: String? = null,
        pendingAvatarBytes: ByteArray? = null,
        updatedAt: Long = 0L,
        displayNamePendingSync: Boolean = false,
        avatarPendingSync: Boolean = false,
    ): ProfileEntity = ProfileEntity(
        id = USER_ID,
        displayName = displayName,
        avatarUrl = avatarUrl,
        pendingAvatarBytes = pendingAvatarBytes,
        updatedAt = updatedAt,
        displayNamePendingSync = displayNamePendingSync,
        avatarPendingSync = avatarPendingSync,
    )

    private companion object {
        const val USER_ID = "user-1"
        const val AVATAR_URL = "https://storage.example/avatar.jpg"
    }
}
