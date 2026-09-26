package com.quare.bibleplanner.core.daystudy.data.mapper

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel

internal class DayStudyPhaseMapper {
    fun map(phase: String): DayStudyPhaseModel? = when (phase) {
        "reading" -> DayStudyPhaseModel.READING
        "chapters" -> DayStudyPhaseModel.CHAPTERS
        "context" -> DayStudyPhaseModel.CONTEXT
        "questions" -> DayStudyPhaseModel.QUESTIONS
        else -> null
    }
}
