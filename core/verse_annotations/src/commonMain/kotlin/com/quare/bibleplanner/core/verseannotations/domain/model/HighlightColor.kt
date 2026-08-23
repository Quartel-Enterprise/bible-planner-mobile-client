package com.quare.bibleplanner.core.verseannotations.domain.model

private const val CUSTOM_PREFIX = "c"
private const val KEY_SEPARATOR = ":"
private const val CUSTOM_KEY_PARTS = 3

/**
 * A highlight colour, identified by the [key] that is stored and synced.
 *
 * A custom colour carries its own HSL components in the key instead of pointing at a palette entry,
 * so a highlight made on one device renders on another that never had that colour in its palette.
 */
sealed interface HighlightColor {
    val key: String

    data class Preset(
        val preset: PresetHighlightColor,
    ) : HighlightColor {
        override val key: String = preset.key
    }

    data class Custom(
        val hue: Int,
        val lightness: Int,
    ) : HighlightColor {
        override val key: String = listOf(CUSTOM_PREFIX, hue, lightness).joinToString(KEY_SEPARATOR)
    }

    companion object {
        fun fromKey(key: String): HighlightColor? {
            val preset = PresetHighlightColor.entries.find { it.key == key }
            if (preset != null) return Preset(preset)
            val parts = key.split(KEY_SEPARATOR)
            if (parts.size != CUSTOM_KEY_PARTS || parts.first() != CUSTOM_PREFIX) return null
            val hue = parts[1].toIntOrNull() ?: return null
            val lightness = parts[2].toIntOrNull() ?: return null
            return Custom(
                hue = hue,
                lightness = lightness,
            )
        }
    }
}
