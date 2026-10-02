package com.quare.bibleplanner.core.chapterstudy.domain.model

data class ChapterStudyModel(
    val summary: String,
    val context: String,
    val outline: List<OutlineSectionModel>,
    val peopleAndPlaces: List<String>,
    val keyVerse: KeyVerseModel?,
    val crossReferences: List<CrossReferenceModel>,
    val reflectionQuestions: List<String>,
)
