package com.quare.bibleplanner.feature.profile.presentation.content

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
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.presentation.content.component.AppSection
import com.quare.bibleplanner.feature.profile.presentation.content.component.CurrentAppVersionText
import com.quare.bibleplanner.feature.profile.presentation.content.component.DeleteDataSection
import com.quare.bibleplanner.feature.profile.presentation.content.component.LegalSection
import com.quare.bibleplanner.feature.profile.presentation.content.component.LoginCard
import com.quare.bibleplanner.feature.profile.presentation.content.component.LogoutButton
import com.quare.bibleplanner.feature.profile.presentation.content.component.PreferencesSection
import com.quare.bibleplanner.feature.profile.presentation.content.component.SocialSection
import com.quare.bibleplanner.feature.profile.presentation.content.component.SupportSection
import com.quare.bibleplanner.feature.profile.presentation.content.component.WebSection
import com.quare.bibleplanner.feature.profile.presentation.content.component.headerSection
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@OptIn(ExperimentalSharedTransitionApi::class)
internal fun ResponsiveContentScope.profileScreenLandscapeLayout(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    responsiveItem {
        LoginCard(
            accountStatusModel = state.accountStatusModel,
            onEvent = onEvent,
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
                if (state.isInstagramLinkVisible.valueOrNull() == true) {
                    SocialSection(
                        onEvent = onEvent,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SupportSection(
                    onEvent = onEvent,
                )
                if (state.isWebAppVisible.valueOrNull() == true) {
                    WebSection(
                        onEvent = onEvent,
                    )
                }
                AppSection(
                    state = state,
                    onEvent = onEvent,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
                DeleteDataSection(
                    onEvent = onEvent,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.accountStatusModel is AccountStatusModel.LoggedIn) {
                        LogoutButton(onClick = { onEvent(ProfileUiEvent.OnLogoutClick) })
                    }
                    LegalSection(
                        onEvent = onEvent,
                    )
                    CurrentAppVersionText(appVersion = state.appVersion)
                }
            }
        }
    }
}
