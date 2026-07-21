package com.quare.bibleplanner.feature.profile.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.profile.presentation.content.profileScreenLandscapeLayout
import com.quare.bibleplanner.feature.profile.presentation.content.profileScreenPortraitLayout
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import com.quare.bibleplanner.ui.utils.LocalMainPadding

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val mainPadding = LocalMainPadding.current

    key(state.selectedLanguage) {
        ResponsiveColumn(
            modifier = Modifier.padding(16.dp),
            contentPadding = mainPadding,
            portraitContent = {
                profileScreenPortraitLayout(
                    state = state,
                    onEvent = onEvent,
                    becomeProTitleContent = becomeProTitleContent,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
            },
            landscapeContent = {
                profileScreenLandscapeLayout(
                    state = state,
                    onEvent = onEvent,
                    becomeProTitleContent = becomeProTitleContent,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
            },
        )
    }
}
