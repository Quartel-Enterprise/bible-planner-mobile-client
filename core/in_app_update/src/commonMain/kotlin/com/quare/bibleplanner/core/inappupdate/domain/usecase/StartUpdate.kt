package com.quare.bibleplanner.core.inappupdate.domain.usecase

fun interface StartUpdate {
    suspend operator fun invoke()
}
