package com.quare.bibleplanner.feature.verse.share.presentation.model

data class ShareCardCustomColor(
    val hue: Int,
    val lightness: Int,
) {
    companion object {
        const val SATURATION = 0.5f
        const val MIN_LIGHTNESS = 20
        const val MAX_LIGHTNESS = 80
    }
}
