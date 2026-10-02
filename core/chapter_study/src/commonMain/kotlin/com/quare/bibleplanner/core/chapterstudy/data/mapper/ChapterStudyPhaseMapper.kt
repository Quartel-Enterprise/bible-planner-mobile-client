package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyPhaseModel

internal class ChapterStudyPhaseMapper {
    fun mapOrNull(phase: String): ChapterStudyPhaseModel? = when (phase) {
        "reading" -> ChapterStudyPhaseModel.READING
        "summary" -> ChapterStudyPhaseModel.SUMMARY
        "context" -> ChapterStudyPhaseModel.CONTEXT
        "questions" -> ChapterStudyPhaseModel.QUESTIONS
        else -> null
    }
}
