package com.quare.bibleplanner.feature.profile.presentation.content

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.profile.presentation.content.component.AnnotationsCard
import com.quare.bibleplanner.feature.profile.presentation.content.component.LoginCard
import com.quare.bibleplanner.feature.profile.presentation.content.component.headerSection
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

internal fun ResponsiveContentScope.profileTopSection(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit,
) {
    responsiveItem {
        LoginCard(
            accountStatusModel = state.accountStatusModel,
            onEvent = onEvent,
        )
    }
    item { VerticalSpacer() }
    responsiveItem {
        AnnotationsCard(
            summary = state.annotationsSummary,
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.ANNOTATIONS)) },
        )
    }
    item { VerticalSpacer() }
    headerSection(
        state = state,
        onEvent = onEvent,
        becomeProTitleContent = becomeProTitleContent,
    )
    item { VerticalSpacer() }
}
