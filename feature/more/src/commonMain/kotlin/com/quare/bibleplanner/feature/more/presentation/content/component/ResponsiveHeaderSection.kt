package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_premium
import bibleplanner.feature.more.generated.resources.become_premium_subtitle
import bibleplanner.feature.more.generated.resources.donate_option
import bibleplanner.feature.more.generated.resources.donate_subtitle
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import org.jetbrains.compose.resources.stringResource

internal fun ResponsiveContentScope.headerSection(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
) {
    if (state.isFreeUser || state.shouldShowDonateOption) {
        state.headerRes?.let { headerRes ->
            responsiveItem {
                SectionHeader(stringResource(headerRes))
            }
        }
        responsiveItem {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isFreeUser) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.become_premium),
                        subtitle = stringResource(Res.string.become_premium_subtitle),
                        icon = Icons.Default.Star,
                        onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.BECOME_PREMIUM)) },
                    )
                }
                if (state.shouldShowDonateOption) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.donate_option),
                        subtitle = stringResource(Res.string.donate_subtitle),
                        icon = Icons.Default.Favorite,
                        onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.DONATE)) },
                    )
                }
            }
        }
    }
}
