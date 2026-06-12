package com.quare.bibleplanner.core.provider.platform

sealed interface Platform {
    data object Android : Platform

    data object Ios : Platform

    sealed interface Desktop : Platform {
        data object MacOs : Desktop

        data object Linux : Desktop

        data object Windows : Desktop
    }
}
