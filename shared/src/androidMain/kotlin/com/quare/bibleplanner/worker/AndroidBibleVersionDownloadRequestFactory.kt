package com.quare.bibleplanner.worker

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.workDataOf

internal class AndroidBibleVersionDownloadRequestFactory {
    fun create(versionId: String): OneTimeWorkRequest = OneTimeWorkRequestBuilder<BibleVersionDownloadWorker>()
        .setInputData(workDataOf(BibleVersionDownloadWorker.KEY_VERSION_ID to versionId))
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setConstraints(
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        ).build()
}
