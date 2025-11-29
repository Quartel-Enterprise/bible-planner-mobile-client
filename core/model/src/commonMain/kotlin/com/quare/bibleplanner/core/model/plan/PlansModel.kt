package com.quare.bibleplanner.core.model.plan

data class PlansModel(
    val chronologicalOrder: List<WeekPlanModel>,
    val booksOrder: List<WeekPlanModel>,
)
