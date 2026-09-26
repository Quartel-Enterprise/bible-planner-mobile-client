package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.AddCustomHighlightColorUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveHighlightPaletteUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeHighlightPaletteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HighlightPaletteUseCasesTest {
    private val existingColor = HighlightColor.Custom(
        hue = 10,
        lightness = 50,
    )
    private val newColor = HighlightColor.Custom(
        hue = 200,
        lightness = 70,
    )
    private lateinit var addCustomHighlightColor: AddCustomHighlightColorUseCase
    private lateinit var observeHighlightPalette: ObserveHighlightPaletteUseCase

    @BeforeTest
    fun setUp() {
        val repository = FakeHighlightPaletteRepository(initialColors = listOf(existingColor))
        addCustomHighlightColor = AddCustomHighlightColorUseCase(repository)
        observeHighlightPalette = ObserveHighlightPaletteUseCase(repository)
    }

    @Test
    fun `observes the custom colours of the palette`() = runTest {
        // When
        val palette = observeHighlightPalette().first()

        // Then
        assertEquals(
            expected = listOf(existingColor),
            actual = palette,
        )
    }

    @Test
    fun `an added colour shows up in the palette`() = runTest {
        // When
        addCustomHighlightColor(newColor)

        // Then
        assertEquals(
            expected = listOf(existingColor, newColor),
            actual = observeHighlightPalette().first(),
        )
    }
}
