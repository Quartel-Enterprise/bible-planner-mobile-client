package com.quare.bibleplanner.feature.daystudy.presentation

import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import org.jetbrains.compose.resources.getPluralString

internal suspend fun getWaitForGenerationsMessage(count: Int): String =
    getPluralString(Res.plurals.ai_study_wait_for_generations, count, count)
