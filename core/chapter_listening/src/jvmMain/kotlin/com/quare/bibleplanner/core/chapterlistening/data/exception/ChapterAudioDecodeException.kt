package com.quare.bibleplanner.core.chapterlistening.data.exception

internal class ChapterAudioDecodeException(
    cause: Throwable?,
) : IllegalStateException("Could not read the chapter audio file", cause)
