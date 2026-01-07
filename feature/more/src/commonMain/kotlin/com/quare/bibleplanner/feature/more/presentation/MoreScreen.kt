package com.quare.bibleplanner.feature.more.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.more.presentation.content.moreScreenLandscapeLayout
import com.quare.bibleplanner.feature.more.presentation.content.moreScreenPortraitLayout
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.SubscriptionDetailsDialog
import com.quare.bibleplanner.ui.component.ResponsiveContent
import com.quare.bibleplanner.ui.utils.LocalMainPadding

@Composable
internal fun MoreScreen(
    state: MoreUiState,
    onEvent: (MoreUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current

    when (state) {
        MoreUiState.Loading -> {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is MoreUiState.Loaded -> {
            if (state.showSubscriptionDetailsDialog) {
                SubscriptionDetailsDialog(
                    onDismiss = { onEvent(MoreUiEvent.OnDismissSubscriptionDetailsDialog) },
                )
            }
            ResponsiveContent(
                modifier = Modifier.padding(16.dp),
                contentPadding = mainPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                portraitContent = {
                    moreScreenPortraitLayout(
                        state = state,
                        onEvent = onEvent,
                    )
                },
                landscapeContent = {
                    moreScreenLandscapeLayout(
                        state = state,
                        onEvent = onEvent,
                    )
                },
            )
        }
    }
}
