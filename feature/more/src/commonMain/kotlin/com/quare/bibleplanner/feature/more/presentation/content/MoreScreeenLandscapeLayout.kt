package com.quare.bibleplanner.feature.more.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
internal fun ResponsiveContentScope.moreScreenLandscapeLayout(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit,
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
    responsiveItem {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PreferencesSection(
                    state = state,
                    onEvent = onEvent,
                )
                if (state.isInstagramLinkVisible) {
                    SocialSection(
                        onEvent = onEvent,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.isWebAppVisible) {
                    WebSection(
                        onEvent = onEvent,
                    )
                }
                DataSection(
                    onEvent = onEvent,
                )
                AppSection(
                    onEvent = onEvent,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LegalSection(
                        onEvent = onEvent,
                    )
                    CurrentAppVersionText(appVersion = state.appVersion)
                }
            }
        }
    }
}
