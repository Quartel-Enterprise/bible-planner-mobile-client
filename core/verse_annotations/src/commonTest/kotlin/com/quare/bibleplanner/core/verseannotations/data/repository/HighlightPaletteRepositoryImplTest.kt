package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.provider.room.entity.HighlightPaletteColorEntity
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.fake.FakeHighlightPaletteColorDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HighlightPaletteRepositoryImplTest {
    private lateinit var repository: HighlightPaletteRepositoryImpl
    private lateinit var dao: FakeHighlightPaletteColorDao

    @Test
    fun `observes the custom colours from the oldest to the newest`() = runTest {
        // Given
        prepareScenario(
            initialColors = listOf(
                paletteEntity(
                    hue = 200,
                    createdAt = 2L,
                ),
                paletteEntity(
                    hue = 100,
                    createdAt = 1L,
                ),
            ),
        )

        // When
        val colors = repository.observeCustomColors().first()

        // Then
        assertEquals(
            expected = listOf(
                HighlightColor.Custom(
                    hue = 100,
                    lightness = LIGHTNESS,
                ),
                HighlightColor.Custom(
                    hue = 200,
                    lightness = LIGHTNESS,
                ),
            ),
            actual = colors,
        )
    }

    @Test
    fun `adds a colour stamped with the current time`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.add(
            HighlightColor.Custom(
                hue = 30,
                lightness = LIGHTNESS,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                paletteEntity(
                    hue = 30,
                    createdAt = NOW,
                ),
            ),
            actual = dao.colors.value,
        )
    }

    @Test
    fun `keeps only the four most recent colours`() = runTest {
        // Given
        prepareScenario(
            initialColors = (1..4).map { index ->
                paletteEntity(
                    hue = index * 10,
                    createdAt = index.toLong(),
                )
            },
        )

        // When
        repository.add(
            HighlightColor.Custom(
                hue = 50,
                lightness = LIGHTNESS,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(20, 30, 40, 50),
            actual = dao.getPaletteColors().map { it.hue },
        )
    }

    @Test
    fun `removes a colour by its key`() = runTest {
        // Given
        prepareScenario(
            initialColors = listOf(
                paletteEntity(
                    hue = 10,
                    createdAt = 1L,
                ),
                paletteEntity(
                    hue = 20,
                    createdAt = 2L,
                ),
            ),
        )

        // When
        repository.remove(
            HighlightColor
                .Custom(
                    hue = 10,
                    lightness = LIGHTNESS,
                ).key,
        )

        // Then
        assertEquals(
            expected = listOf(20),
            actual = dao.colors.value.map { it.hue },
        )
    }

    private fun paletteEntity(
        hue: Int,
        createdAt: Long,
    ): HighlightPaletteColorEntity = HighlightPaletteColorEntity(
        colorKey = HighlightColor
            .Custom(
                hue = hue,
                lightness = LIGHTNESS,
            ).key,
        hue = hue,
        lightness = LIGHTNESS,
        createdAtEpochMillis = createdAt,
    )

    private fun prepareScenario(initialColors: List<HighlightPaletteColorEntity> = emptyList()) {
        dao = FakeHighlightPaletteColorDao(initialColors = initialColors)
        repository = HighlightPaletteRepositoryImpl(
            highlightPaletteColorDao = dao,
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val LIGHTNESS = 60
        const val NOW = 5_000L
    }
}
