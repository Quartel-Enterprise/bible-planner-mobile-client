package com.quare.bibleplanner.core.chapterstudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterStudyContentDto(
    @SerialName("summary") val summary: String,
    @SerialName("context") val context: String,
    @SerialName("outline") val outline: List<OutlineSectionDto>,
    @SerialName("peopleAndPlaces") val peopleAndPlaces: List<String>,
    @SerialName("keyVerse") val keyVerse: KeyVerseDto?,
    @SerialName("crossReferences") val crossReferences: List<CrossReferenceDto>,
    @SerialName("reflectionQuestions") val reflectionQuestions: List<String>,
)
