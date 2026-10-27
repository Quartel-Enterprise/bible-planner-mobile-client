package com.quare.bibleplanner.feature.verse.share.presentation.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.luminance
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackgroundUiModel
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardCustomColor
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardPalette
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class ShareCardPaletteMapperTest {
    @Test
    fun `GIVEN every preset WHEN mapping it to a palette THEN uses the colors of the preset`() {
        // Given
        val presets = ShareCardBackground.entries

        // When
        val palettes = presets.map { ShareCardBackgroundUiModel.Preset(it).toPalette() }

        // Then
        assertEquals(
            expected = presets.map {
                ShareCardPalette(
                    startColor = it.startColor,
                    endColor = it.endColor,
                    textColor = it.textColor,
                    subtitleColor = it.subtitleColor,
                )
            },
            actual = palettes,
        )
    }

    @Test
    fun `GIVEN a custom color WHEN mapping it to a palette THEN the gradient starts at the swatch color`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 200,
            lightness = 40,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = color.toSwatchColor(),
            actual = palette.startColor,
        )
    }

    @Test
    fun `GIVEN a custom color WHEN mapping it THEN the gradient ends 12 degrees of hue further and 18 darker`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 200,
            lightness = 40,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = ShareCardCustomColor(
                hue = 212,
                lightness = 22,
            ).toSwatchColor(),
            actual = palette.endColor,
        )
    }

    @Test
    fun `GIVEN a light custom color with dark text WHEN mapping it THEN the gradient ends 8 lighter`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 200,
            lightness = 62,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = ShareCardCustomColor(
                hue = 212,
                lightness = 70,
            ).toSwatchColor(),
            actual = palette.endColor,
        )
    }

    @Test
    fun `GIVEN the lightest custom color WHEN mapping it THEN the gradient end stays at lightness 90`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 200,
            lightness = 88,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = ShareCardCustomColor(
                hue = 212,
                lightness = 90,
            ).toSwatchColor(),
            actual = palette.endColor,
        )
    }

    @Test
    fun `GIVEN a very dark custom color WHEN mapping it THEN the gradient end stays at lightness 8`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 100,
            lightness = 20,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = ShareCardCustomColor(
                hue = 112,
                lightness = 8,
            ).toSwatchColor(),
            actual = palette.endColor,
        )
    }

    @Test
    fun `GIVEN a hue near the end of the wheel WHEN mapping it THEN the shifted end hue wraps around`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 355,
            lightness = 40,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = ShareCardCustomColor(
                hue = 7,
                lightness = 22,
            ).toSwatchColor(),
            actual = palette.endColor,
        )
    }

    @Test
    fun `GIVEN a light custom color at lightness 62 WHEN mapping it to a palette THEN uses dark text`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 50,
            lightness = 62,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertNotEquals(
            illegal = Color.White,
            actual = palette.textColor,
        )
        assertTrue(palette.textColor.luminance() < DARK_LUMINANCE)
    }

    @Test
    fun `GIVEN a custom color at lightness 61 WHEN mapping it to a palette THEN uses white text`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 50,
            lightness = 61,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = Color.White,
            actual = palette.textColor,
        )
    }

    @Test
    fun `GIVEN a light custom color WHEN mapping it THEN the subtitle is the text color with less alpha`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 50,
            lightness = 80,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = palette.textColor.copy(alpha = 1f),
            actual = palette.subtitleColor.copy(alpha = 1f),
        )
        assertTrue(palette.subtitleColor.alpha < palette.textColor.alpha)
    }

    @Test
    fun `GIVEN a dark custom color WHEN mapping it to a palette THEN the subtitle is white with reduced alpha`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 250,
            lightness = 30,
        )

        // When
        val palette = ShareCardBackgroundUiModel.Custom(color).toPalette()

        // Then
        assertEquals(
            expected = Color.White,
            actual = palette.subtitleColor.copy(alpha = 1f),
        )
        assertTrue(palette.subtitleColor.alpha < 1f)
    }

    @Test
    fun `GIVEN a photo background WHEN mapping it THEN uses white text over a scrim darker at the bottom`() {
        // Given
        val background = ShareCardBackgroundUiModel.Photo(FakeImageBitmap())

        // When
        val palette = background.toPalette()

        // Then
        assertEquals(
            expected = Color.White,
            actual = palette.textColor,
        )
        assertEquals(
            expected = Color.White,
            actual = palette.subtitleColor.copy(alpha = 1f),
        )
        assertTrue(palette.subtitleColor.alpha < 1f)
        assertEquals(
            expected = Color.Black,
            actual = palette.startColor.copy(alpha = 1f),
        )
        assertEquals(
            expected = Color.Black,
            actual = palette.endColor.copy(alpha = 1f),
        )
        assertTrue(palette.startColor.alpha < palette.endColor.alpha)
        assertTrue(palette.endColor.alpha < 1f)
    }

    @Test
    fun `GIVEN a red hue at mid lightness WHEN mapping it to a swatch color THEN returns an opaque red`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 0,
            lightness = 50,
        )

        // When
        val swatch = color.toSwatchColor()

        // Then
        assertEquals(
            expected = 1f,
            actual = swatch.alpha,
        )
        assertTrue(swatch.red > swatch.green)
        assertTrue(swatch.red > swatch.blue)
        assertEquals(
            expected = swatch.green,
            actual = swatch.blue,
            absoluteTolerance = COLOR_TOLERANCE,
        )
    }

    @Test
    fun `GIVEN full lightness WHEN mapping it to a swatch color THEN returns white`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 180,
            lightness = 100,
        )

        // When
        val swatch = color.toSwatchColor()

        // Then
        assertEquals(
            expected = Color.White,
            actual = swatch,
        )
    }

    @Test
    fun `GIVEN zero lightness WHEN mapping it to a swatch color THEN returns black`() {
        // Given
        val color = ShareCardCustomColor(
            hue = 180,
            lightness = 0,
        )

        // When
        val swatch = color.toSwatchColor()

        // Then
        assertEquals(
            expected = Color.Black,
            actual = swatch,
        )
    }

    private companion object {
        const val DARK_LUMINANCE = 0.2f
        const val COLOR_TOLERANCE = 0.001f
    }
}

private class FakeImageBitmap : ImageBitmap {
    override val width: Int = 1
    override val height: Int = 1
    override val colorSpace: ColorSpace get() = error("unused")
    override val hasAlpha: Boolean get() = error("unused")
    override val config: ImageBitmapConfig get() = error("unused")

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ) = error("unused")

    override fun prepareToDraw() = error("unused")
}
