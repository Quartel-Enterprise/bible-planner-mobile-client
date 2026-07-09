package com.quare.bibleplanner.feature.more.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.more.presentation.content.component.ContactSupportDialogContent
import com.quare.bibleplanner.feature.more.presentation.content.moreScreenLandscapeLayout
import com.quare.bibleplanner.feature.more.presentation.content.moreScreenPortraitLayout
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.SubscriptionDetailsDialog
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.utils.LocalMainPadding

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MoreScreen(
    state: MoreUiState,
    onEvent: (MoreUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val mainPadding = LocalMainPadding.current

    key(state.selectedLanguage) {
        if (state.showSubscriptionDetailsDialog) {
            SubscriptionDetailsDialog(
                onDismiss = { onEvent(MoreUiEvent.OnDismissSubscriptionDetailsDialog) },
            )
        }
        if (state.showContactSupportDialog) {
            ResponsiveDialogSheet(
                onCloseClick = { onEvent(MoreUiEvent.OnDismissContactSupportDialog) },
                skipPartiallyExpanded = true,
            ) {
                ContactSupportDialogContent(state = state, onEvent = onEvent)
            }
        }
        ResponsiveColumn(
            modifier = Modifier.padding(16.dp),
            contentPadding = mainPadding,
            portraitContent = {
                moreScreenPortraitLayout(
                    state = state,
                    onEvent = onEvent,
                    becomeProTitleContent = becomeProTitleContent,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
            },
            landscapeContent = {
                moreScreenLandscapeLayout(
                    state = state,
                    onEvent = onEvent,
                    becomeProTitleContent = becomeProTitleContent,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
            },
        )
    }
}
