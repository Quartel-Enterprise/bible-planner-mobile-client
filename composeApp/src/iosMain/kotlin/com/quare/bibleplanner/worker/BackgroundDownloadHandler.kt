package com.quare.bibleplanner.worker

interface BackgroundDownloadHandler {
    fun onResume(onComplete: (Boolean) -> Unit)
    fun onExpire(onComplete: () -> Unit)
}
