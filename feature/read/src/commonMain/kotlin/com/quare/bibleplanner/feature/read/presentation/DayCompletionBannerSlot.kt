package com.quare.bibleplanner.feature.read.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel

fun interface DayCompletionBannerSlot {
    @Composable
    fun Content(
        day: PlanDayLocationModel,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
    )
}
