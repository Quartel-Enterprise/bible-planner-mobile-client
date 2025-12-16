package com.quare.bibleplanner.feature.readingplan.presentation.component.fabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.go_to_unread
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoToUnreadFab(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(visible = isVisible) {
        ExtendedFloatingActionButton(
            modifier = modifier,
            expanded = isExpanded && isVisible,
            text = {
                Text(stringResource(Res.string.go_to_unread))
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                )
            },
            onClick = onClick,
        )
    }
}
