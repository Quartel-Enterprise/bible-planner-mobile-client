package com.quare.bibleplanner.worker

interface IosDownloadSession {
    fun setBridge(bridge: IosBackgroundDownloadBridge)
    fun addDownloadTask(url: String, versionId: String, chapterId: Long)
    fun cancelDownloads(versionId: String)
    fun cancelAllDownloads()
}
