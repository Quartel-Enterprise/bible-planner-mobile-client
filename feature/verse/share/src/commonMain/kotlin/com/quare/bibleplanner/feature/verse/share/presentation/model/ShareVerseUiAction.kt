package com.quare.bibleplanner.feature.verse.share.presentation.model

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel

sealed interface ShareVerseUiAction {
    data object NavigateBack : ShareVerseUiAction

    data class NavigateToRoute(
        val route: NavKey,
    ) : ShareVerseUiAction

    /**
     * Carries the passage rather than a finished message: the wording around it is localized, and
     * resources are resolved in the UI layer.
     */
    data class ShareText(
        val content: VersesShareContentModel,
    ) : ShareVerseUiAction

    data class ShareImage(
        val content: VersesShareContentModel,
        val imageBytes: ByteArray,
    ) : ShareVerseUiAction {
        override fun equals(other: Any?): Boolean = this === other ||
            (other is ShareImage && content == other.content && imageBytes.contentEquals(other.imageBytes))

        override fun hashCode(): Int = 31 * content.hashCode() + imageBytes.contentHashCode()
    }
}
