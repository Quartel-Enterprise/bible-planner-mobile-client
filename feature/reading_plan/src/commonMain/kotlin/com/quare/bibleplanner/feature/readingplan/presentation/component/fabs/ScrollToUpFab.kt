package com.quare.bibleplanner.feature.readingplan.presentation.component.fabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.scroll_to_top
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.icons.Icon
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScrollToUpFab(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
        ) {
            Icon(
                icon = AppIcon.KeyboardArrowUp,
                contentDescription = stringResource(Res.string.scroll_to_top),
            )
        }
    }
}
