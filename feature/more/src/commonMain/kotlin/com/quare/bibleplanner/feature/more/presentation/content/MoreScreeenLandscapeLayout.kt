package com.quare.bibleplanner.feature.more.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.more.presentation.content.component.DataSection
import com.quare.bibleplanner.feature.more.presentation.content.component.LegalSection
import com.quare.bibleplanner.feature.more.presentation.content.component.PreferencesSection
import com.quare.bibleplanner.feature.more.presentation.content.component.SocialSection
import com.quare.bibleplanner.feature.more.presentation.content.component.headerSection
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope

internal fun ResponsiveContentScope.moreScreenLandscapeLayout(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
) {
    headerSection(state = state, onEvent = onEvent)

    responsiveItem {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreferencesSection(
                modifier = Modifier.weight(1f),
                state = state,
                onEvent = onEvent,
            )
            LegalSection(
                modifier = Modifier.weight(1f),
                onEvent = onEvent,
            )
        }
    }

    responsiveItem {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DataSection(
                modifier = Modifier.weight(1f),
                onEvent = onEvent,
            )
            if (state.isInstagramLinkVisible) {
                SocialSection(
                    modifier = Modifier.weight(1f),
                    onEvent = onEvent,
                )
            } else {
                // Keep the weight to maintain alignment of the Data section
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
