package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

/*
 * Why: a reader opened because the player moved on must not pull the player back to its own
 * chapter, which is what a reader the person opened does. Only the latest request is kept and the
 * next reader clears it whatever its chapter, so a navigation that never landed cannot linger.
 */
internal class ListeningFollowRequests {
    private var requestedChapter: ChapterLocationModel? = null

    fun request(chapter: ChapterLocationModel) {
        requestedChapter = chapter
    }

    fun consume(chapter: ChapterLocationModel): Boolean {
        val isRequested = requestedChapter == chapter
        requestedChapter = null
        return isRequested
    }
}
