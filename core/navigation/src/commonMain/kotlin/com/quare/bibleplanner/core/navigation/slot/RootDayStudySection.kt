package com.quare.bibleplanner.core.navigation.slot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.presentation.DayStudySectionSlot
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudySection

internal object RootDayStudySection : DayStudySectionSlot {
    @Composable
    override fun Content(
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
        onOpenPaywall: () -> Unit,
        onOpenLoginWarning: () -> Unit,
        onShowSnackBar: (String) -> Unit,
        onNavigateToStudy: () -> Unit,
        modifier: Modifier,
    ) {
        DayStudySection(
            passages = passages,
            dayRoute = dayRoute,
            onOpenPaywall = onOpenPaywall,
            onOpenLoginWarning = onOpenLoginWarning,
            onShowSnackBar = onShowSnackBar,
            onNavigateToStudy = onNavigateToStudy,
            modifier = modifier,
        )
    }
}
