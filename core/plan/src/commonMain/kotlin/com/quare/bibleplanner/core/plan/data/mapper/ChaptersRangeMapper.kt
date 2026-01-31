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
                    // Start a new range
                    currentRangeStart = chapter
                    currentRangeEnd = chapter
                }

                canGroupChapters(currentRangeEnd!!, chapter) -> {
                    // Extend the current range
                    currentRangeEnd = chapter
                }

                else -> {
                    // Finish current range and start a new one
                    ranges.add(formatChapterRange(currentRangeStart, currentRangeEnd))
                    currentRangeStart = chapter
                    currentRangeEnd = chapter
                }
            }
        }

        // Add the last range
        if (currentRangeStart != null) {
            ranges.add(formatChapterRange(currentRangeStart, currentRangeEnd))
        }

        ranges.joinToString(", ")
    }

    private fun canGroupChapters(
        first: ChapterModel,
        second: ChapterModel,
    ): Boolean {
        // Can only group if chapters are consecutive
        if (second.number != first.number + 1) return false

        // Can group if neither has specific verse ranges
        val firstHasVerses = first.startVerse != null || first.endVerse != null
        val secondHasVerses = second.startVerse != null || second.endVerse != null

        // Only group if both don't have verses (full chapters)
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
                // Single chapter
                if (startVerseStr != null) {
                    "${start.number}:$startVerseStr"
                } else {
                    "${start.number}"
                }
            }

            startVerseStr != null && endVerseStr != null -> {
                // Range with verses: "5:1-10-12:5-8"
                "${start.number}:$startVerseStr-${endChapter.number}:$endVerseStr"
            }

            startVerseStr != null -> {
                // Start has verses, end doesn't: "5:1-10-12"
                "${start.number}:$startVerseStr-${endChapter.number}"
            }

            endVerseStr != null -> {
                // End has verses, start doesn't: "5-12:5-8"
                "${start.number}-${endChapter.number}:$endVerseStr"
            }

            else -> {
                // No verses: "5-12"
                "${start.number}-${endChapter.number}"
            }
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

        startVerse != null -> {
            "$startVerse"
        }

        endVerse != null -> {
            "-$endVerse"
        }

        else -> {
            null
        }
    }
}
