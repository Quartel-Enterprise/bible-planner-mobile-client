package com.quare.bibleplanner.feature.verse.share.presentation.model

import androidx.compose.ui.graphics.ImageBitmap

sealed interface ShareCardBackgroundUiModel {
    val analyticsKey: String

    data class Preset(
        val background: ShareCardBackground,
    ) : ShareCardBackgroundUiModel {
        override val analyticsKey: String = background.name.lowercase()
    }

    data class Custom(
        val color: ShareCardCustomColor,
    ) : ShareCardBackgroundUiModel {
        override val analyticsKey: String = "custom"
    }

    data class Photo(
        val image: ImageBitmap,
    ) : ShareCardBackgroundUiModel {
        override val analyticsKey: String = "photo"
    }
}
