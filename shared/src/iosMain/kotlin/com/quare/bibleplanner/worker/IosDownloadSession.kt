package com.quare.bibleplanner.worker

interface IosDownloadSession {
    fun setBridge(bridge: IosBackgroundDownloadBridge)

    fun addDownloadTask(
        url: String,
        versionId: String,
        chapterId: Long,
    )

    fun cancelDownloads(versionId: String)

    fun cancelAllDownloads()

    fun startLiveActivity(
        versionId: String,
        versionName: String,
    )

    fun pauseLiveActivity(versionId: String)

    fun resumeLiveActivity(versionId: String)

    fun endLiveActivity(versionId: String)

    /** Called after all addDownloadTask calls for a version are complete. */
    fun notifyAllTasksRegistered(
        versionId: String,
        totalTaskCount: Int,
    )
}
