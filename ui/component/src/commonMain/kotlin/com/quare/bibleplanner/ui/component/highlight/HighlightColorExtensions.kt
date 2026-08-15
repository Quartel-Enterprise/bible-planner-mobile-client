package com.quare.bibleplanner.ui.component.highlight

import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor

private const val CUSTOM_SATURATION = 0.8f
private const val CUSTOM_BACKGROUND_ALPHA = 0.35f
private const val PERCENT = 100f

/** The colour of the palette swatch: the full-strength ink of the highlight. */
fun HighlightColor.toSwatchColor(): Color = when (this) {
    is HighlightColor.Preset -> preset.swatchColor
    is HighlightColor.Custom -> customColor(alpha = 1f)
}

/** The wash drawn behind the verse text, translucent so the text stays readable in both themes. */
fun HighlightColor.toBackgroundColor(): Color = when (this) {
    is HighlightColor.Preset -> preset.swatchColor.copy(alpha = preset.backgroundAlpha)
    is HighlightColor.Custom -> customColor(alpha = CUSTOM_BACKGROUND_ALPHA)
}

private fun HighlightColor.Custom.customColor(alpha: Float): Color = Color.hsl(
    hue = hue.toFloat(),
    saturation = CUSTOM_SATURATION,
    lightness = lightness / PERCENT,
    alpha = alpha,
)

private val PresetHighlightColor.swatchColor: Color
    get() = when (this) {
        PresetHighlightColor.TEAL -> Color(0xFF26C6DA)
        PresetHighlightColor.GREEN -> Color(0xFF66BB6A)
        PresetHighlightColor.YELLOW -> Color(0xFFE0CC30)
        PresetHighlightColor.PINK -> Color(0xFFEC6FA8)
        PresetHighlightColor.ORANGE -> Color(0xFFFF9E5E)
        PresetHighlightColor.PURPLE -> Color(0xFFB388F5)
    }

private val PresetHighlightColor.backgroundAlpha: Float
    get() = when (this) {
        PresetHighlightColor.TEAL, PresetHighlightColor.GREEN, PresetHighlightColor.PINK -> 0.30f
        PresetHighlightColor.YELLOW -> 0.35f
        PresetHighlightColor.ORANGE, PresetHighlightColor.PURPLE -> 0.32f
    }
