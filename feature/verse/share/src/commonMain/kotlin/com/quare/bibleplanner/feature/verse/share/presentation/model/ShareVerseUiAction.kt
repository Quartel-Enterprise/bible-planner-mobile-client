package com.quare.bibleplanner.feature.verse.share.presentation.model

import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import org.jetbrains.compose.resources.StringResource

sealed interface ShareVerseUiAction {
    /*
     * Why: carries the passage, not a finished message, because the surrounding wording is
     * localized and resources are resolved in the UI layer.
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

    data class SaveImage(
        val imageBytes: ByteArray,
        val fileName: String,
    ) : ShareVerseUiAction {
        override fun equals(other: Any?): Boolean = this === other ||
            (other is SaveImage && fileName == other.fileName && imageBytes.contentEquals(other.imageBytes))

        override fun hashCode(): Int = 31 * fileName.hashCode() + imageBytes.contentHashCode()
    }

    data object LaunchPhotoPicker : ShareVerseUiAction

    data class ShowMessage(
        val stringResource: StringResource,
    ) : ShareVerseUiAction
}
