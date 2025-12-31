package com.quare.bibleplanner.core.model.plan

import kotlinx.datetime.LocalDate

data class DayModel(
    val number: Int,
    val passages: List<PassagePlanModel>,
    val isRead: Boolean,
    val totalVerses: Int,
    val readVerses: Int,
    val readTimestamp: Long?,
    val plannedReadDate: LocalDate?,
    val notes: String?,
)
