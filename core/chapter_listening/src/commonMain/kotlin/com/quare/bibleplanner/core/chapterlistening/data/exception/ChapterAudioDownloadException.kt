package com.quare.bibleplanner.core.chapterlistening.data.exception

internal class ChapterAudioDownloadException(
    val statusCode: Int,
) : IllegalStateException("The chapter audio download responded with status $statusCode")
