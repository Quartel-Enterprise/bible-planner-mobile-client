package com.quare.bibleplanner.core.chapterstudy.fake

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyContentDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyResponseDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.CrossReferenceDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.KeyVerseDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.OutlineSectionDto

// The wire form of the createChapterStudy() fixture of :core:chapter_study:testing.
internal fun chapterStudyResponse(cacheToken: String): ChapterStudyResponseDto = ChapterStudyResponseDto(
    content = ChapterStudyContentDto(
        summary = "The serpent leads the woman to doubt the word of God.",
        context = "Chapter 3 explains where sin and death come from.",
        outline = listOf(
            OutlineSectionDto(
                startVerse = 1,
                endVerse = 7,
                title = "The temptation and the fall",
            ),
            OutlineSectionDto(
                startVerse = 8,
                endVerse = 24,
                title = "The judgement of God",
            ),
        ),
        peopleAndPlaces = listOf("The serpent", "Eve", "Garden of Eden"),
        keyVerse = KeyVerseDto(
            startVerse = 15,
            endVerse = 15,
            note = "The first promise of the Redeemer",
        ),
        crossReferences = listOf(
            CrossReferenceDto(
                book = "ROMANS",
                chapter = 5,
                startVerse = 12,
                endVerse = 19,
            ),
        ),
        reflectionQuestions = listOf("Where do you tend to doubt God's goodness?"),
    ),
    model = "model-x",
    promptVersion = 2,
    updatedAt = "2026-10-01T10:00:00Z",
    isPro = false,
    clientCacheToken = cacheToken,
)
