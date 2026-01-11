package com.quare.bibleplanner.feature.more.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.more.presentation.content.component.AppSection
import com.quare.bibleplanner.feature.more.presentation.content.component.DataSection
import com.quare.bibleplanner.feature.more.presentation.content.component.LegalSection
import com.quare.bibleplanner.feature.more.presentation.content.component.PreferencesSection
import com.quare.bibleplanner.feature.more.presentation.content.component.SocialSection
import com.quare.bibleplanner.feature.more.presentation.content.component.WebSection
import com.quare.bibleplanner.feature.more.presentation.content.component.headerSection
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope

@OptIn(ExperimentalSharedTransitionApi::class)
internal fun ResponsiveContentScope.moreScreenPortraitLayout(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    headerSection(
        state = state,
        onEvent = onEvent,
        becomeProTitleContent = becomeProTitleContent,
    )
    responsiveItem { PreferencesSection(state = state, onEvent = onEvent) }
    responsiveItem { DataSection(onEvent = onEvent) }
    if (state.isWebAppVisible) {
        responsiveItem { WebSection(onEvent = onEvent) }
    }
    responsiveItem {
        AppSection(
            onEvent = onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
    responsiveItem { LegalSection(onEvent = onEvent) }
    if (state.isInstagramLinkVisible) {
        responsiveItem { SocialSection(onEvent = onEvent) }
    }
}
