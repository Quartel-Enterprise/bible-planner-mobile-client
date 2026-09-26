package com.quare.bibleplanner.core.profile.data.sync

import com.quare.bibleplanner.core.profile.data.dto.ProfileDto
import com.quare.bibleplanner.core.profile.data.mapper.ProfileMapper
import com.quare.bibleplanner.core.profile.fake.FakeProfileDao
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ProfileLocalStoreTest {
    private val remoteUpdatedAtMillis = Instant.parse(REMOTE_UPDATED_AT).toEpochMilliseconds()
    private lateinit var localStore: ProfileLocalStore
    private lateinit var dao: FakeProfileDao

    @Test
    fun `observes and reads only the profiles with a pending change`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "pending",
                    displayNamePendingSync = true,
                ),
                entity(id = "synced"),
            ),
        )

        // When
        val observed = localStore.observePending().first()
        val read = localStore.getPending()

        // Then
        assertEquals(
            expected = listOf("pending"),
            actual = observed.map { it.id },
        )
        assertEquals(
            expected = observed,
            actual = read,
        )
    }

    @Test
    fun `clears the pushed name flag but keeps the avatar pending while its bytes wait for upload`() = runTest {
        // Given
        val pending = entity(
            id = USER_ID,
            pendingAvatarBytes = byteArrayOf(1),
            displayNamePendingSync = true,
            avatarPendingSync = true,
        )
        prepareScenario(initialRows = listOf(pending))

        // When
        localStore.markSynced(pending)

        // Then
        assertEquals(
            expected = listOf(pending.copy(displayNamePendingSync = false)),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `clears the avatar flag once its uploaded url was pushed`() = runTest {
        // Given
        val pending = entity(
            id = USER_ID,
            avatarUrl = "https://storage.example/avatar.jpg",
            avatarPendingSync = true,
        )
        prepareScenario(initialRows = listOf(pending))

        // When
        localStore.markSynced(pending)

        // Then
        assertEquals(
            expected = listOf(pending.copy(avatarPendingSync = false)),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `inserts a remote profile this device has never seen`() = runTest {
        // Given
        prepareScenario()

        // When
        localStore.applyRemote(dto(displayName = "Remote Name"))

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    id = USER_ID,
                    displayName = "Remote Name",
                    updatedAt = remoteUpdatedAtMillis,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `overwrites an older synced profile with a newer remote one`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = USER_ID,
                    displayName = "Old Name",
                ),
            ),
        )

        // When
        localStore.applyRemote(dto(displayName = "Remote Name"))

        // Then
        assertEquals(
            expected = "Remote Name",
            actual = dao.rows.value
                .single()
                .displayName,
        )
    }

    @Test
    fun `keeps a pending local edit over a remote one`() = runTest {
        // Given
        val pending = entity(
            id = USER_ID,
            displayName = "Local Name",
            displayNamePendingSync = true,
        )
        prepareScenario(initialRows = listOf(pending))

        // When
        localStore.applyRemote(dto(displayName = "Remote Name"))

        // Then
        assertEquals(
            expected = listOf(pending),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `builds the remote payload for the signed in user`() {
        // Given
        prepareScenario()

        // When
        val dto = localStore.toDto(
            userId = USER_ID,
            entity = entity(
                id = USER_ID,
                displayName = "Name",
                updatedAt = 0L,
                displayNamePendingSync = true,
            ),
        )

        // Then
        assertEquals(
            expected = ProfileDto(
                id = USER_ID,
                displayName = "Name",
                avatarUrl = null,
                updatedAt = "1970-01-01T00:00:00Z",
                isDisplayNameDirty = true,
                isAvatarDirty = false,
            ),
            actual = dto,
        )
    }

    @Test
    fun `clears every local profile`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(entity(id = USER_ID)))

        // When
        localStore.clearLocal()

        // Then
        assertTrue(dao.rows.value.isEmpty())
    }

    private fun dto(displayName: String?): ProfileDto = ProfileDto(
        id = USER_ID,
        displayName = displayName,
        avatarUrl = null,
        updatedAt = REMOTE_UPDATED_AT,
        isDisplayNameDirty = false,
        isAvatarDirty = false,
    )

    private fun entity(
        id: String,
        displayName: String? = null,
        avatarUrl: String? = null,
        pendingAvatarBytes: ByteArray? = null,
        updatedAt: Long = OLD_TIMESTAMP,
        displayNamePendingSync: Boolean = false,
        avatarPendingSync: Boolean = false,
    ): ProfileEntity = ProfileEntity(
        id = id,
        displayName = displayName,
        avatarUrl = avatarUrl,
        pendingAvatarBytes = pendingAvatarBytes,
        updatedAt = updatedAt,
        displayNamePendingSync = displayNamePendingSync,
        avatarPendingSync = avatarPendingSync,
    )

    private fun prepareScenario(initialRows: List<ProfileEntity> = emptyList()) {
        dao = FakeProfileDao(initialRows = initialRows)
        localStore = ProfileLocalStore(
            profileDao = dao,
            profileMapper = ProfileMapper(),
        )
    }

    private companion object {
        const val USER_ID = "user-id"
        const val OLD_TIMESTAMP = 100L
        const val REMOTE_UPDATED_AT = "2026-07-11T10:00:00Z"
    }
}
