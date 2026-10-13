package com.quare.bibleplanner.notification

interface IosDownloadLiveActivity {
    fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    )

    fun showPaused(versionId: String)

    fun showComplete(versionId: String)

    fun dismiss(versionId: String)
}
