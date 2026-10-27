package com.quare.bibleplanner.feature.verse.share.presentation.mapper

import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackgroundUiModel
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardCustomColor
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardPalette

private const val PERCENT = 100f
private const val FULL_TURN = 360
private const val CUSTOM_END_HUE_SHIFT = 12
private const val CUSTOM_END_DARKENING = 18
private const val CUSTOM_MIN_END_LIGHTNESS = 8
private const val CUSTOM_END_LIGHTENING = 8
private const val CUSTOM_MAX_END_LIGHTNESS = 90
private const val DARK_TEXT_FROM_LIGHTNESS = 62
private const val DARK_TEXT_SATURATION = 0.35f
private const val DARK_TEXT_LIGHTNESS = 0.16f
private const val LIGHT_SUBTITLE_ALPHA = 0.72f
private const val DARK_SUBTITLE_ALPHA = 0.65f
private val photoScrimTop = Color(0x33000000)
private val photoScrimBottom = Color(0xA6000000)

internal fun ShareCardBackgroundUiModel.toPalette(): ShareCardPalette = when (this) {
    is ShareCardBackgroundUiModel.Preset -> ShareCardPalette(
        startColor = background.startColor,
        endColor = background.endColor,
        textColor = background.textColor,
        subtitleColor = background.subtitleColor,
    )

    is ShareCardBackgroundUiModel.Custom -> color.toPalette()

    // Why: the gradient is drawn over the photo as a scrim, so white text reads on any picture.
    is ShareCardBackgroundUiModel.Photo -> ShareCardPalette(
        startColor = photoScrimTop,
        endColor = photoScrimBottom,
        textColor = Color.White,
        subtitleColor = Color.White.copy(alpha = LIGHT_SUBTITLE_ALPHA),
    )
}

internal fun ShareCardCustomColor.toSwatchColor(): Color = createHslColor(
    hue = hue,
    lightness = lightness,
)

/*
 * Why: dark text sits on light colors, so their gradient turns lighter instead of darker, or the
 * reference and mark at the darker end would lose contrast.
 */
private fun ShareCardCustomColor.toPalette(): ShareCardPalette {
    val hasDarkText = lightness >= DARK_TEXT_FROM_LIGHTNESS
    val textColor = if (hasDarkText) {
        Color.hsl(
            hue = hue.toFloat(),
            saturation = DARK_TEXT_SATURATION,
            lightness = DARK_TEXT_LIGHTNESS,
        )
    } else {
        Color.White
    }
    val subtitleAlpha = if (hasDarkText) DARK_SUBTITLE_ALPHA else LIGHT_SUBTITLE_ALPHA
    val endLightness = if (hasDarkText) {
        (lightness + CUSTOM_END_LIGHTENING).coerceAtMost(CUSTOM_MAX_END_LIGHTNESS)
    } else {
        (lightness - CUSTOM_END_DARKENING).coerceAtLeast(CUSTOM_MIN_END_LIGHTNESS)
    }
    return ShareCardPalette(
        startColor = toSwatchColor(),
        endColor = createHslColor(
            hue = (hue + CUSTOM_END_HUE_SHIFT) % FULL_TURN,
            lightness = endLightness,
        ),
        textColor = textColor,
        subtitleColor = textColor.copy(alpha = subtitleAlpha),
    )
}

private fun createHslColor(
    hue: Int,
    lightness: Int,
): Color = Color.hsl(
    hue = hue.toFloat(),
    saturation = ShareCardCustomColor.SATURATION,
    lightness = lightness / PERCENT,
)
