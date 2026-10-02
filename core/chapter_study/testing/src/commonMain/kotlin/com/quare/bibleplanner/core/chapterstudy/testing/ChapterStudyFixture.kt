package com.quare.bibleplanner.core.chapterstudy.testing

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.CrossReferenceModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.KeyVerseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.OutlineSectionModel
import com.quare.bibleplanner.core.model.book.BookId

fun createChapterStudy(): ChapterStudyModel = ChapterStudyModel(
    summary = "The serpent leads the woman to doubt the word of God.",
    context = "Chapter 3 explains where sin and death come from.",
    outline = listOf(
        OutlineSectionModel(
            startVerse = 1,
            endVerse = 7,
            title = "The temptation and the fall",
        ),
        OutlineSectionModel(
            startVerse = 8,
            endVerse = 24,
            title = "The judgement of God",
        ),
    ),
    peopleAndPlaces = listOf("The serpent", "Eve", "Garden of Eden"),
    keyVerse = KeyVerseModel(
        startVerse = 15,
        endVerse = 15,
        note = "The first promise of the Redeemer",
    ),
    crossReferences = listOf(
        CrossReferenceModel(
            bookId = BookId.ROM,
            chapterNumber = 5,
            startVerse = 12,
            endVerse = 19,
        ),
    ),
    reflectionQuestions = listOf("Where do you tend to doubt God's goodness?"),
)
