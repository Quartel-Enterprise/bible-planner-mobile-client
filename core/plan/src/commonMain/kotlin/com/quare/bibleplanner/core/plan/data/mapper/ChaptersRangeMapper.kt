package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.model.plan.ChapterModel

class ChaptersRangeMapper {
    fun map(chapters: List<ChapterModel>): String = chapters.run {
        if (isEmpty()) return ""

        val sortedChapters = sortedBy { it.number }
        val ranges = mutableListOf<String>()

        var currentRangeStart: ChapterModel? = null
        var currentRangeEnd: ChapterModel? = null

        for (chapter in sortedChapters) {
            when {
                currentRangeStart == null -> {
                    currentRangeStart = chapter
                    currentRangeEnd = chapter
                }

                canGroupChapters(currentRangeEnd!!, chapter) -> currentRangeEnd = chapter

                else -> {
                    ranges.add(formatChapterRange(currentRangeStart, currentRangeEnd))
                    currentRangeStart = chapter
                    currentRangeEnd = chapter
                }
            }
        }

        if (currentRangeStart != null) {
            ranges.add(formatChapterRange(currentRangeStart, currentRangeEnd))
        }

        ranges.joinToString(", ")
    }

    private fun canGroupChapters(
        first: ChapterModel,
        second: ChapterModel,
    ): Boolean {
        if (second.number != first.number + 1) return false

        val firstHasVerses = first.startVerse != null || first.endVerse != null
        val secondHasVerses = second.startVerse != null || second.endVerse != null

        return !firstHasVerses && !secondHasVerses
    }

    private fun formatChapterRange(
        start: ChapterModel,
        end: ChapterModel?,
    ): String {
        val endChapter = end ?: start
        val startVerseStr = formatVerseRange(start.startVerse, start.endVerse)
        val endVerseStr = formatVerseRange(endChapter.startVerse, endChapter.endVerse)

        return when {
            start.number == endChapter.number -> {
                if (startVerseStr != null) {
                    "${start.number}:$startVerseStr"
                } else {
                    "${start.number}"
                }
            }

            startVerseStr != null && endVerseStr != null ->
                "${start.number}:$startVerseStr-${endChapter.number}:$endVerseStr"

            startVerseStr != null -> "${start.number}:$startVerseStr-${endChapter.number}"

            endVerseStr != null -> "${start.number}-${endChapter.number}:$endVerseStr"

            else -> "${start.number}-${endChapter.number}"
        }
    }

    private fun formatVerseRange(
        startVerse: Int?,
        endVerse: Int?,
    ): String? = when {
        startVerse != null && endVerse != null -> {
            if (startVerse == endVerse) {
                "$startVerse"
            } else {
                "$startVerse-$endVerse"
            }
        }

        startVerse != null -> "$startVerse"

        endVerse != null -> "-$endVerse"

        else -> null
    }
}
