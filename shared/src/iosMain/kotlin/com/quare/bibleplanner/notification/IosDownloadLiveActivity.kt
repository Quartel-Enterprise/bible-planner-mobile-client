package com.quare.bibleplanner.notification

interface IosDownloadLiveActivity {
    fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    )

    fun showPaused(
        versionId: String,
        onShown: () -> Unit,
    )

    fun showComplete(versionId: String)

    fun dismiss(versionId: String)

    fun keepOnly(versionIds: List<String>)
}
