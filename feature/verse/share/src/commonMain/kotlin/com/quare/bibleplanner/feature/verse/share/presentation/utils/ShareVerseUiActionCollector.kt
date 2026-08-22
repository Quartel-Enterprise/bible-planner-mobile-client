package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.utils.shareContent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ShareVerseUiActionCollector(
    uiActionFlow: Flow<ShareVerseUiAction>,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            ShareVerseUiAction.NavigateBack -> onNavigateBack()

            is ShareVerseUiAction.NavigateToRoute -> onNavigate(uiAction.route)

            is ShareVerseUiAction.ShareText -> shareContent(uiAction.content.shareText)

            is ShareVerseUiAction.ShareImage -> {
                shareContent(
                    message = uiAction.content.shareText,
                    imageBytes = uiAction.imageBytes,
                )
            }
        }
    }
}
