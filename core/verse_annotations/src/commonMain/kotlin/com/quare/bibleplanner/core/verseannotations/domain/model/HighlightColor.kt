package com.quare.bibleplanner.core.verseannotations.domain.model

private const val CUSTOM_PREFIX = "c"
private const val KEY_SEPARATOR = ":"
private const val CUSTOM_KEY_PARTS = 3

// Why: a custom colour carries its HSL components in the key so a highlight renders on devices
// whose palette never had that colour.
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
