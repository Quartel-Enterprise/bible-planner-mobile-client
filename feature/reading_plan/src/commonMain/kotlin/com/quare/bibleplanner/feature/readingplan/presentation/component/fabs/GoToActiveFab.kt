package com.quare.bibleplanner.feature.readingplan.presentation.component.fabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.fab_back_to_today
import bibleplanner.feature.reading_plan.generated.resources.fab_next_reading
import bibleplanner.feature.reading_plan.generated.resources.fab_resume
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.icons.Icon
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoToActiveFab(
    isVisible: Boolean,
    planMode: PlanMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = planMode.toFabConfig() ?: return
    AnimatedVisibility(visible = isVisible) {
        ExtendedFloatingActionButton(
            modifier = modifier,
            text = { Text(stringResource(config.labelResource)) },
            icon = {
                Icon(
                    icon = config.icon,
                    contentDescription = null,
                )
            },
            onClick = onClick,
        )
    }
}

private data class FabConfig(
    val labelResource: StringResource,
    val icon: AppIcon,
)

private fun PlanMode.toFabConfig(): FabConfig? {
    val (labelResource, icon) = when (this) {
        PlanMode.Ahead, PlanMode.CaughtUp -> Res.string.fab_next_reading to AppIcon.ArrowForward
        PlanMode.Behind -> Res.string.fab_resume to AppIcon.History
        PlanMode.New, PlanMode.OnTrack -> Res.string.fab_back_to_today to AppIcon.Today
        PlanMode.Done -> return null
    }
    return FabConfig(labelResource = labelResource, icon = icon)
}
