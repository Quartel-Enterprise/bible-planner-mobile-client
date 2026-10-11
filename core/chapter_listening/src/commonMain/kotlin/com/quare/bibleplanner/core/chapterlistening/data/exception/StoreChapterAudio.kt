package com.quare.bibleplanner.core.chapterlistening.data.exception

import kotlinx.io.IOException

// Why: a full disk must read as a storage failure, not as the missing connection a failed download means.
internal inline fun <T> storeChapterAudio(block: () -> T): T = try {
    block()
} catch (exception: IOException) {
    throw ChapterAudioStorageException(exception)
}
