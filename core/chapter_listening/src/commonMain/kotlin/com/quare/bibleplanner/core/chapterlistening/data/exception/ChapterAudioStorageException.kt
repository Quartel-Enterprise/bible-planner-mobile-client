package com.quare.bibleplanner.core.chapterlistening.data.exception

internal class ChapterAudioStorageException(
    cause: Throwable,
) : IllegalStateException("Could not store the chapter audio on the device", cause)
