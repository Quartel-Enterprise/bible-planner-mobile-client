package com.quare.bibleplanner.feature.inappupdate.domain.usecase

fun interface StartUpdate {
    suspend operator fun invoke()
}
