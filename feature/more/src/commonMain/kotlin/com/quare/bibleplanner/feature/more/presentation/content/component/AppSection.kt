package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.app_section
import bibleplanner.feature.more.generated.resources.check_for_updates_checking
import bibleplanner.feature.more.generated.resources.check_for_updates_subtitle
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun AppSection(
    state: MoreUiState,
    onEvent: (MoreUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.app_section))
        SectionCard {
            if (state.isUpdateRowVisible) {
                MoreMenuItem(
                    itemModel = MoreMenuOptionsFactory.checkForUpdate,
                    subtitle = if (state.isCheckingForUpdate) {
                        stringResource(Res.string.check_for_updates_checking)
                    } else {
                        stringResource(Res.string.check_for_updates_subtitle, state.appVersion)
                    },
                    onClick = {
                        if (!state.isCheckingForUpdate) {
                            onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.CHECK_FOR_UPDATE))
                        }
                    },
                    trailingContent = if (state.isCheckingForUpdate) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    } else {
                        null
                    },
                )
                HorizontalDivider()
            }
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.releaseNotes,
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.RELEASE_NOTES)) },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                sharedElementKey = "release_notes_card",
            )
        }
    }
}
