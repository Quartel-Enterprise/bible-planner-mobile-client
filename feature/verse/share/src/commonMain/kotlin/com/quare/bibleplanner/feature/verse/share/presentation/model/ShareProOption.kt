package com.quare.bibleplanner.feature.verse.share.presentation.model

enum class ShareProOption {
    CUSTOM_COLOR,
    PHOTO,
    WATERMARK,
    ;

    val key: String
        get() = name.lowercase()
}
