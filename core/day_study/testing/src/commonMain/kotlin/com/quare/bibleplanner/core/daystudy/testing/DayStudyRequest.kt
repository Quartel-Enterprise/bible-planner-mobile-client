package com.quare.bibleplanner.core.daystudy.testing

import com.quare.bibleplanner.core.model.plan.PassageModel

data class DayStudyRequest(
    val passages: List<PassageModel>,
    val version: String,
    val languageCode: String,
)
