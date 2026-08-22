package com.quare.bibleplanner.core.verseannotations.domain.model

enum class PresetHighlightColor(
    val key: String,
) {
    TEAL("teal"),
    GREEN("green"),
    YELLOW("yellow"),
    PINK("pink"),
    ORANGE("orange"),
    PURPLE("purple"),
    ;

    val requiresPro: Boolean
        get() = ordinal >= FREE_PRESET_COUNT

    private companion object {
        const val FREE_PRESET_COUNT = 3
    }
}
