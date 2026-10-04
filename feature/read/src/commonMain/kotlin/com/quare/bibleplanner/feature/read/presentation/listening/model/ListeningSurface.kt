package com.quare.bibleplanner.feature.read.presentation.listening.model

enum class ListeningSurface {
    MINI_PLAYER,
    PLAYER,
    ;

    val key: String
        get() = name.lowercase()
}
