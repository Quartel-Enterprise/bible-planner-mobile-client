package com.quare.bibleplanner.core.inappupdate.domain.usecase

fun interface InstallDownloadedUpdate {
    suspend operator fun invoke()
}
