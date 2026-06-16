package com.quare.bibleplanner.worker

import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.workDataOf

internal class AndroidBibleVersionDownloadRequestFactory {
    // Expedited so the download starts with a background FGS-start exemption; otherwise the worker can
    // run while backgrounded, where setForeground() is refused on Android 12+.
    fun create(versionId: String): OneTimeWorkRequest = OneTimeWorkRequestBuilder<BibleVersionDownloadWorker>()
        .setInputData(workDataOf(BibleVersionDownloadWorker.KEY_VERSION_ID to versionId))
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
}
