package com.quare.bibleplanner.feature.bibleversion.domain

class IncompleteBibleDownloadException(
    versionId: String,
    downloadedChapters: Int,
    totalChapters: Int,
) : IllegalStateException("$versionId has $downloadedChapters of $totalChapters chapters after downloading")
