package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_message
import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.utils.shareContent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

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

            is ShareVerseUiAction.ShareText -> shareContent(uiAction.content.toMessage())

            is ShareVerseUiAction.ShareImage -> {
                shareContent(
                    message = uiAction.content.toMessage(),
                    imageBytes = uiAction.imageBytes,
                )
            }
        }
    }
}

private suspend fun VersesShareContentModel.toMessage(): String = getString(
    Res.string.share_message,
    text,
    reference,
    versionName,
)
