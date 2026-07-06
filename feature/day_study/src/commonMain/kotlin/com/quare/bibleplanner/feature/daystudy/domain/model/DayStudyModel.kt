package com.quare.bibleplanner.feature.daystudy.domain.model

data class DayStudyModel(
    val passageLabel: String,
    val overview: String,
    val chapterSummaries: List<ChapterSummaryModel>,
    val takeaways: List<String>,
    val context: HistoricalContextModel,
    val commonQuestions: List<QaModel>,
)
