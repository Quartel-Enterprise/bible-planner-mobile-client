package com.quare.bibleplanner.core.navigation.slot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.DayReadingCompleteBanner
import com.quare.bibleplanner.feature.read.presentation.DayCompletionBannerSlot

internal object RootDayCompletionBanner : DayCompletionBannerSlot {
    @Composable
    override fun Content(
        day: PlanDayLocationModel,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
    ) {
        DayReadingCompleteBanner(
            day = day,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
        )
    }
}
