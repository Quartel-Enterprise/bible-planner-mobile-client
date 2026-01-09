package com.quare.bibleplanner.feature.more.presentation.content

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.more.presentation.content.component.DataSection
import com.quare.bibleplanner.feature.more.presentation.content.component.LegalSection
import com.quare.bibleplanner.feature.more.presentation.content.component.PreferencesSection
import com.quare.bibleplanner.feature.more.presentation.content.component.SocialSection
import com.quare.bibleplanner.feature.more.presentation.content.component.WebSection
import com.quare.bibleplanner.feature.more.presentation.content.component.headerSection
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope

internal fun ResponsiveContentScope.moreScreenPortraitLayout(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit = {},
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
    responsiveItem { LegalSection(onEvent = onEvent) }
    if (state.isInstagramLinkVisible) {
        responsiveItem { SocialSection(onEvent = onEvent) }
    }
}
