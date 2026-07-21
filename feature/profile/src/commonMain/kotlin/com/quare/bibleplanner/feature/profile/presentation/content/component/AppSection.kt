package com.quare.bibleplanner.feature.profile.presentation.content.component

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
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.app_section
import bibleplanner.feature.profile.generated.resources.check_for_updates_checking
import bibleplanner.feature.profile.generated.resources.check_for_updates_subtitle
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileMenuOptionsFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun AppSection(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.app_section))
        SectionCard {
            if (state.isUpdateRowVisible) {
                ProfileMenuItem(
                    itemModel = ProfileMenuOptionsFactory.checkForUpdate,
                    subtitle = if (state.isCheckingForUpdate) {
                        stringResource(Res.string.check_for_updates_checking)
                    } else {
                        stringResource(Res.string.check_for_updates_subtitle, state.appVersion)
                    },
                    onClick = {
                        if (!state.isCheckingForUpdate) {
                            onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))
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
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.releaseNotes,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.RELEASE_NOTES)) },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                sharedElementKey = "release_notes_card",
            )
        }
    }
}
