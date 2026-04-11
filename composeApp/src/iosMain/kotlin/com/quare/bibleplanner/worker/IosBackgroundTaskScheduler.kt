package com.quare.bibleplanner.worker

interface IosBackgroundTaskScheduler {
    fun scheduleDownload()
    fun cancelDownload()
    fun setHandler(handler: BackgroundDownloadHandler?)
}
