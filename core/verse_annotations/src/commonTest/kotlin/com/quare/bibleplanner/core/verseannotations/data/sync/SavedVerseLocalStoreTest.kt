package com.quare.bibleplanner.core.verseannotations.data.sync

import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import com.quare.bibleplanner.core.verseannotations.data.dto.SavedVerseDto
import com.quare.bibleplanner.core.verseannotations.data.mapper.SavedVerseMapper
import com.quare.bibleplanner.core.verseannotations.data.mapper.SyncTimestampMapper
import com.quare.bibleplanner.core.verseannotations.fake.FakeSavedVerseDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class SavedVerseLocalStoreTest {
    private lateinit var localStore: SavedVerseLocalStore
    private lateinit var dao: FakeSavedVerseDao

    @Test
    fun `observes and reads only the pending rows`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    isPendingSync = true,
                ),
                entity(
                    verseNumber = 2,
                    isPendingSync = false,
                ),
            ),
        )

        // When
        val observed = localStore.observePending().first()
        val read = localStore.getPending()

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    verseNumber = 1,
                    isPendingSync = true,
                ),
            ),
            actual = observed,
        )
        assertEquals(
            expected = observed,
            actual = read,
        )
    }

    @Test
    fun `marks a pushed row synced when it was not touched meanwhile`() = runTest {
        // Given
        val pending = entity(
            verseNumber = 1,
            isPendingSync = true,
        )
        prepareScenario(initialRows = listOf(pending))

        // When
        localStore.markSynced(pending)

        // Then
        assertEquals(
            expected = listOf(pending.copy(isPendingSync = false)),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `keeps a row pending when it was edited again during the push`() = runTest {
        // Given
        val pushed = entity(
            verseNumber = 1,
            isPendingSync = true,
        )
        val reEdited = pushed.copy(updatedAtEpochMillis = LOCAL_UPDATED_AT + 1)
        prepareScenario(initialRows = listOf(reEdited))

        // When
        localStore.markSynced(pushed)

        // Then
        assertEquals(
            expected = listOf(reEdited),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `inserts a remote row this device has never seen`() = runTest {
        // Given
        prepareScenario()

        // When
        localStore.applyRemote(
            dto(
                isSaved = true,
                updatedAt = REMOTE_UPDATED_AT,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    verseNumber = 1,
                    isSaved = true,
                    updatedAt = Instant.parse(REMOTE_UPDATED_AT).toEpochMilliseconds(),
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `overwrites an older synced row with a newer remote change`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    isSaved = true,
                ),
            ),
        )

        // When
        localStore.applyRemote(
            dto(
                isSaved = false,
                updatedAt = REMOTE_UPDATED_AT,
            ),
        )

        // Then
        assertFalse(
            dao.rows.value
                .single()
                .isSaved,
        )
    }

    @Test
    fun `keeps a pending local change over a remote one`() = runTest {
        // Given
        val pending = entity(
            verseNumber = 1,
            isSaved = false,
            isPendingSync = true,
        )
        prepareScenario(initialRows = listOf(pending))

        // When
        localStore.applyRemote(
            dto(
                isSaved = true,
                updatedAt = REMOTE_UPDATED_AT,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(pending),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `builds the remote payload with an ISO timestamp`() {
        // Given
        prepareScenario()

        // When
        val dto = localStore.toDto(
            userId = USER_ID,
            entity = entity(
                verseNumber = 1,
                isSaved = true,
                updatedAt = 0L,
            ),
        )

        // Then
        assertEquals(
            expected = dto(
                isSaved = true,
                updatedAt = "1970-01-01T00:00:00Z",
            ),
            actual = dto,
        )
    }

    @Test
    fun `clears every local row`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    isPendingSync = true,
                ),
            ),
        )

        // When
        localStore.clearLocal()

        // Then
        assertTrue(dao.rows.value.isEmpty())
    }

    private fun dto(
        isSaved: Boolean,
        updatedAt: String,
    ): SavedVerseDto = SavedVerseDto(
        userId = USER_ID,
        bibleVersionId = "ACF",
        bookId = "GEN",
        chapterNumber = 1,
        verseNumber = 1,
        isSaved = isSaved,
        updatedAt = updatedAt,
    )

    private fun entity(
        verseNumber: Int,
        isSaved: Boolean = true,
        updatedAt: Long = LOCAL_UPDATED_AT,
        isPendingSync: Boolean = false,
    ): SavedVerseEntity = SavedVerseEntity(
        bibleVersionId = "ACF",
        bookId = "GEN",
        chapterNumber = 1,
        verseNumber = verseNumber,
        isSaved = isSaved,
        updatedAtEpochMillis = updatedAt,
        isPendingSync = isPendingSync,
    )

    private fun prepareScenario(initialRows: List<SavedVerseEntity> = emptyList()) {
        dao = FakeSavedVerseDao(initialRows = initialRows)
        localStore = SavedVerseLocalStore(
            savedVerseDao = dao,
            savedVerseMapper = SavedVerseMapper(SyncTimestampMapper()),
        )
    }

    private companion object {
        const val USER_ID = "user-1"
        const val LOCAL_UPDATED_AT = 100L
        const val REMOTE_UPDATED_AT = "2026-07-11T10:00:00Z"
    }
}
