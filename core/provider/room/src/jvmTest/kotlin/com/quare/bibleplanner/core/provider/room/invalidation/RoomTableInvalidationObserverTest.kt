package com.quare.bibleplanner.core.provider.room.invalidation

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.HighlightPaletteColorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class RoomTableInvalidationObserverTest {
    private val waitTimeout = 5.seconds
    private lateinit var database: AppDatabase
    private lateinit var observer: RoomTableInvalidationObserver

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        observer = RoomTableInvalidationObserver(database)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN an observed table WHEN it is written THEN emits once right away and once for the write`() = runTest {
        // When
        val emissions = withContext(Dispatchers.Default) {
            withTimeout(waitTimeout) {
                val signals = mutableListOf<Unit>()
                val collector = launch {
                    observer(TABLE).take(2).toList(signals)
                }
                while (signals.isEmpty()) yield()
                database.highlightPaletteColorDao().upsertPaletteColor(
                    HighlightPaletteColorEntity(
                        colorKey = "c:10:50",
                        hue = 10,
                        lightness = 50,
                        createdAtEpochMillis = 1L,
                    ),
                )
                collector.join()
                signals
            }
        }

        // Then
        assertEquals(
            expected = 2,
            actual = emissions.size,
        )
    }

    private companion object {
        const val TABLE = "highlight_palette_colors"
    }
}
