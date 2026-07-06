package com.quare.bibleplanner.feature.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DayStudyContentDto(
    @SerialName("passageLabel") val passageLabel: String,
    @SerialName("overview") val overview: String,
    @SerialName("chapterSummaries") val chapterSummaries: List<ChapterSummaryDto>,
    @SerialName("takeaways") val takeaways: List<String>,
    @SerialName("context") val context: HistoricalContextDto,
    @SerialName("commonQuestions") val commonQuestions: List<QaDto>,
)
