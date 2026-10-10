package com.quare.bibleplanner.core.provider.room.transaction

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RoomDatabaseTransactionRunnerTest {
    private lateinit var database: AppDatabase
    private lateinit var runInTransaction: RoomDatabaseTransactionRunner

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        runInTransaction = RoomDatabaseTransactionRunner(database)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN writes across DAOs WHEN the block completes THEN commits them`() = runTest {
        // When
        runInTransaction {
            database.bookDao().insertBooks(listOf(book(BOOK_ID)))
        }

        // Then
        assertEquals(
            expected = BOOK_ID,
            actual = database.bookDao().getBookById(BOOK_ID)?.id,
        )
    }

    @Test
    fun `GIVEN a block that fails after writing WHEN running it THEN rolls the writes back`() = runTest {
        // When
        suspendRunCatching {
            runInTransaction {
                database.bookDao().insertBooks(listOf(book(BOOK_ID)))
                error("seeding interrupted")
            }
        }

        // Then
        assertNull(database.bookDao().getBookById(BOOK_ID))
    }

    private fun book(id: String): BookEntity = BookEntity(
        id = id,
        isRead = false,
        isFavorite = false,
        favoriteUpdatedAt = null,
        isFavoritePendingSync = false,
    )

    private companion object {
        const val BOOK_ID = "GEN"
    }
}
