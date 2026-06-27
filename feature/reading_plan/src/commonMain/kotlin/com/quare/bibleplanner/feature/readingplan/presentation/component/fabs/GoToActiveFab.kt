package com.quare.bibleplanner.feature.readingplan.presentation.component.fabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.fab_back_to_today
import bibleplanner.feature.reading_plan.generated.resources.fab_next_reading
import bibleplanner.feature.reading_plan.generated.resources.fab_resume
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
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
                    imageVector = config.icon,
                    contentDescription = null,
                )
            },
            onClick = onClick,
        )
    }
}

private data class FabConfig(
    val labelResource: StringResource,
    val icon: ImageVector,
)

private fun PlanMode.toFabConfig(): FabConfig? {
    val (labelResource, icon) = when (this) {
        PlanMode.Ahead, PlanMode.CaughtUp -> Res.string.fab_next_reading to Icons.AutoMirrored.Filled.ArrowForward
        PlanMode.Behind -> Res.string.fab_resume to Icons.Default.History
        PlanMode.New, PlanMode.OnTrack -> Res.string.fab_back_to_today to Icons.Default.Today
        PlanMode.Done -> return null
    }
    return FabConfig(labelResource = labelResource, icon = icon)
}
