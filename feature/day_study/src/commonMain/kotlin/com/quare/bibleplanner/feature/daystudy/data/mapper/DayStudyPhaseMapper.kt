package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel

internal class DayStudyPhaseMapper {
    fun map(phase: String): DayStudyPhaseModel? = when (phase) {
        "reading" -> DayStudyPhaseModel.READING
        "chapters" -> DayStudyPhaseModel.CHAPTERS
        "context" -> DayStudyPhaseModel.CONTEXT
        "questions" -> DayStudyPhaseModel.QUESTIONS
        else -> null
    }
}
