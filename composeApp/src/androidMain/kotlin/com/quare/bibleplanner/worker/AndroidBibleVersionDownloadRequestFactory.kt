package com.quare.bibleplanner.worker

import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf

internal class AndroidBibleVersionDownloadRequestFactory {
    fun create(versionId: String): OneTimeWorkRequest = OneTimeWorkRequestBuilder<BibleVersionDownloadWorker>()
        .setInputData(workDataOf(BibleVersionDownloadWorker.KEY_VERSION_ID to versionId))
        .build()
}
