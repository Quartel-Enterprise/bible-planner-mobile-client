package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.utils.shareContent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ShareVerseUiActionCollector(uiActionFlow: Flow<ShareVerseUiAction>) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
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
