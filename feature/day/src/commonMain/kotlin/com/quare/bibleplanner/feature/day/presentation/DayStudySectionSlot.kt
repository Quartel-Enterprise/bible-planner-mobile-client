package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute

fun interface DayStudySectionSlot {
    @Composable
    fun Content(
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
        onOpenPaywall: () -> Unit,
        onOpenLoginWarning: () -> Unit,
        onShowSnackBar: (String) -> Unit,
        onNavigateToStudy: () -> Unit,
        modifier: Modifier,
    )
}
