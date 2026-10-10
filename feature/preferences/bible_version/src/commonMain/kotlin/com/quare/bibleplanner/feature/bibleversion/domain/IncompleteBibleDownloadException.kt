package com.quare.bibleplanner.feature.bibleversion.domain

class IncompleteBibleDownloadException(
    versionId: String,
    missingChapters: Int,
    totalChapters: Int,
) : IllegalStateException("$versionId is missing $missingChapters of $totalChapters chapters after downloading")
