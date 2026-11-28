package com.quare.bibleplanner.android.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.utils.transition.TransitionScopes
import software.quare.loadable.valueOrNull

// Why: the same breakpoint, width cap and spacing as the shared screen's ResponsiveColumn.
private val twoColumnsMinWidth = 600.dp
private val contentMaxWidth = 800.dp
private val screenPadding = 16.dp
private val sectionSpacing = 16.dp

@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    transitionScopes: TransitionScopes?,
    hostedTabBarHeight: Dp,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Scaffold(
        // Why: in landscape a side display cutout would otherwise cover the cards.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Why: the shared screen measures its breakpoint and cap inside its 16dp padding, so this does too.
            val availableWidth = maxWidth - screenPadding * 2
            val isTwoColumns = availableWidth > twoColumnsMinWidth
            val centeringPadding =
                ((availableWidth - availableWidth.coerceAtMost(contentMaxWidth)) / 2).coerceAtLeast(0.dp)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                /*
                 * Why: the insets go into the content padding instead of around the list, so the profile scrolls
                 * under the status bar edge to edge and its first and last rows still clear the system bars.
                 */
                contentPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection) + screenPadding + centeringPadding,
                    top = innerPadding.calculateTopPadding() + screenPadding,
                    end = innerPadding.calculateEndPadding(layoutDirection) + screenPadding + centeringPadding,
                    // Why: the profile scrolls under the main screen's bar, so its last row clears it while it shows.
                    bottom = innerPadding.calculateBottomPadding() + hostedTabBarHeight + screenPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing),
            ) {
                profileTopItems(
                    state = state,
                    transitionScopes = transitionScopes,
                    onEvent = onEvent,
                )
                if (isTwoColumns) {
                    profileTwoColumnItems(
                        state = state,
                        transitionScopes = transitionScopes,
                        onEvent = onEvent,
                    )
                } else {
                    profileSingleColumnItems(
                        state = state,
                        transitionScopes = transitionScopes,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.profileTopItems(
    state: ProfileUiState,
    transitionScopes: TransitionScopes?,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    item(key = "account") {
        ProfileAccountCard(
            accountStatusModel = state.accountStatusModel,
            onEvent = onEvent,
        )
    }
    item(key = "annotations") {
        ProfileAnnotationsCard(
            summary = state.annotationsSummary,
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.ANNOTATIONS)) },
        )
    }
    item(key = "pro_and_donate") {
        ProfileProAndDonateSection(
            state = state,
            transitionScopes = transitionScopes,
            onEvent = onEvent,
        )
    }
}

private fun LazyListScope.profileSingleColumnItems(
    state: ProfileUiState,
    transitionScopes: TransitionScopes?,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    val isLoggedIn = state.accountStatusModel is AccountStatusModel.LoggedIn
    item(key = "preferences") {
        ProfilePreferencesSection(
            state = state,
            onEvent = onEvent,
        )
    }
    item(key = "support") { ProfileSupportSection(onEvent = onEvent) }
    item(key = "app") {
        ProfileAppSection(
            state = state,
            transitionScopes = transitionScopes,
            onEvent = onEvent,
        )
    }
    if (state.isInstagramLinkVisible.valueOrNull() == true) {
        item(key = "social") { ProfileSocialSection(onEvent = onEvent) }
    }
    item(key = "account_data") {
        ProfileAccountDataSection(
            isLoggedIn = isLoggedIn,
            onEvent = onEvent,
        )
    }
    if (isLoggedIn) {
        item(key = "logout") { ProfileLogoutButton(onClick = { onEvent(ProfileUiEvent.OnLogoutClick) }) }
    }
    item(key = "footer") {
        ProfileFooterColumn(
            state = state,
            onEvent = onEvent,
        )
    }
}

private fun LazyListScope.profileTwoColumnItems(
    state: ProfileUiState,
    transitionScopes: TransitionScopes?,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    val isLoggedIn = state.accountStatusModel is AccountStatusModel.LoggedIn
    item(key = "columns") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing),
            ) {
                ProfilePreferencesSection(
                    state = state,
                    onEvent = onEvent,
                )
                if (state.isInstagramLinkVisible.valueOrNull() == true) {
                    ProfileSocialSection(onEvent = onEvent)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing),
            ) {
                ProfileSupportSection(onEvent = onEvent)
                ProfileAppSection(
                    state = state,
                    transitionScopes = transitionScopes,
                    onEvent = onEvent,
                )
                ProfileAccountDataSection(
                    isLoggedIn = isLoggedIn,
                    onEvent = onEvent,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isLoggedIn) {
                        ProfileLogoutButton(onClick = { onEvent(ProfileUiEvent.OnLogoutClick) })
                    }
                    ProfileFooterColumn(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}
