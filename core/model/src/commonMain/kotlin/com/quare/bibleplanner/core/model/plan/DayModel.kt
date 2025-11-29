package com.quare.bibleplanner.core.model.plan

data class DayModel(
    val number: Int,
    val passages: List<PassagePlanModel>,
    val isRead: Boolean,
    val totalVerses: Int,
    val readVerses: Int,
)
