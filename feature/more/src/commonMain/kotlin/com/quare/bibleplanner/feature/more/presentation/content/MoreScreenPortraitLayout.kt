package com.quare.bibleplanner.feature.more.presentation.content

import com.quare.bibleplanner.feature.more.presentation.content.component.DataSection
import com.quare.bibleplanner.feature.more.presentation.content.component.LegalSection
import com.quare.bibleplanner.feature.more.presentation.content.component.PreferencesSection
import com.quare.bibleplanner.feature.more.presentation.content.component.SocialSection
import com.quare.bibleplanner.feature.more.presentation.content.component.headerSection
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope

internal fun ResponsiveContentScope.moreScreenPortraitLayout(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
) {
    headerSection(state = state, onEvent = onEvent)
    responsiveItem { PreferencesSection(state = state, onEvent = onEvent) }
    responsiveItem { DataSection(onEvent = onEvent) }
    responsiveItem { LegalSection(onEvent = onEvent) }
    if (state.isInstagramLinkVisible) {
        responsiveItem { SocialSection(onEvent = onEvent) }
    }
}
