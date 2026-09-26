package com.quare.bibleplanner.feature.read.fixture

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.feature.read.presentation.DayCompletionBannerSlot

internal object NoDayCompletionBanner : DayCompletionBannerSlot {
    @Composable
    override fun Content(
        day: PlanDayLocationModel,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
    ) = Unit
}
