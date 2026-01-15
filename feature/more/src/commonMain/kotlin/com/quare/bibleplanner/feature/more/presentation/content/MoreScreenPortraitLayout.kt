package com.quare.bibleplanner.feature.more.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.more.presentation.content.component.AppSection
import com.quare.bibleplanner.feature.more.presentation.content.component.CurrentAppVersionText
import com.quare.bibleplanner.feature.more.presentation.content.component.DataSection
import com.quare.bibleplanner.feature.more.presentation.content.component.LegalSection
import com.quare.bibleplanner.feature.more.presentation.content.component.LoginCard
import com.quare.bibleplanner.feature.more.presentation.content.component.PreferencesSection
import com.quare.bibleplanner.feature.more.presentation.content.component.SocialSection
import com.quare.bibleplanner.feature.more.presentation.content.component.WebSection
import com.quare.bibleplanner.feature.more.presentation.content.component.headerSection
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@OptIn(ExperimentalSharedTransitionApi::class)
internal fun ResponsiveContentScope.moreScreenPortraitLayout(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    responsiveItem {
        LoginCard(
            onLoginClick = { onEvent(MoreUiEvent.OnLoginClick) },
        )
    }
    item { VerticalSpacer() }
    headerSection(
        state = state,
        onEvent = onEvent,
        becomeProTitleContent = becomeProTitleContent,
    )
    item { VerticalSpacer() }
    responsiveItem { PreferencesSection(state = state, onEvent = onEvent) }
    item { VerticalSpacer() }
    responsiveItem { DataSection(onEvent = onEvent) }
    if (state.isWebAppVisible) {
        item { VerticalSpacer() }
        responsiveItem { WebSection(onEvent = onEvent) }
    }
    item { VerticalSpacer() }
    responsiveItem {
        AppSection(
            onEvent = onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
    if (state.isInstagramLinkVisible) {
        item { VerticalSpacer() }
        responsiveItem { SocialSection(onEvent = onEvent) }
    }
    item { VerticalSpacer() }
    responsiveItem {
        LegalSection(
            modifier = Modifier.fillMaxWidth(),
            onEvent = onEvent,
        )
    }
    responsiveItem {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CurrentAppVersionText(appVersion = state.appVersion)
        }
    }
}
