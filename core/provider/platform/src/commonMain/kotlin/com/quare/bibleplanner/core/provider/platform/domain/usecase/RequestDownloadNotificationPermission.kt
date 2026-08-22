package com.quare.bibleplanner.core.provider.platform.domain.usecase

fun interface RequestDownloadNotificationPermission {
    suspend operator fun invoke()
}
