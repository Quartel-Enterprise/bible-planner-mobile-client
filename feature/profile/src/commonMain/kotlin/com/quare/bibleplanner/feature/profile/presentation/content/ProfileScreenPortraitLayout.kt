package com.quare.bibleplanner.feature.profile.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
internal fun ResponsiveContentScope.profileScreenPortraitLayout(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit = {},
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
    responsiveItem { PreferencesSection(state = state, onEvent = onEvent) }
    item { VerticalSpacer() }
    responsiveItem { SupportSection(onEvent = onEvent) }
    item { VerticalSpacer() }
    if (state.isWebAppVisible.valueOrNull() == true) {
        responsiveItem { WebSection(onEvent = onEvent) }
        item { VerticalSpacer() }
    }
    responsiveItem {
        AppSection(
            state = state,
            onEvent = onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
    if (state.isInstagramLinkVisible.valueOrNull() == true) {
        item { VerticalSpacer() }
        responsiveItem { SocialSection(onEvent = onEvent) }
    }
    item { VerticalSpacer() }
    responsiveItem { DeleteDataSection(onEvent = onEvent) }
    if (state.accountStatusModel is AccountStatusModel.LoggedIn) {
        item { VerticalSpacer() }
        responsiveItem {
            LogoutButton(onClick = { onEvent(ProfileUiEvent.OnLogoutClick) })
        }
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
