package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.more_options
import bibleplanner.feature.reading_plan.generated.resources.reading_plan
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingPlanTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    isShowingMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(text = stringResource(Res.string.reading_plan))
            }
        },
        scrollBehavior = scrollBehavior,
        actions = {
            CommonIconButton(
                imageVector = Icons.Default.MoreVert,
                onClick = {
                    onEvent(ReadingPlanUiEvent.OnOverflowClick)
                },
                contentDescription = stringResource(Res.string.more_options),
            )
            ReadingPlanDropdownMenu(
                isShowingMenu = isShowingMenu,
                onEvent = onEvent,
            )
        },
    )
}
