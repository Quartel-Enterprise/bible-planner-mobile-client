package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationItemUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent

@Composable
internal fun AnnotationsRemoveDialog(
    item: AnnotationItemUiModel,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    val passage = item.passage
    AppAlertDialog(
        title = stringResource(
            R.string.annotations_remove_title,
            verseReferenceLabel(
                bookId = passage.chapter.bookId,
                chapterNumber = passage.chapter.chapterNumber,
                verseNumbers = passage.verseNumbers,
            ),
        ),
        text = stringResource(R.string.annotations_remove_body),
        confirmText = stringResource(R.string.annotations_remove),
        onConfirm = { onEvent(AnnotationsUiEvent.OnRemoveConfirm(item)) },
        dismissText = stringResource(R.string.annotations_cancel),
        onDismiss = { onEvent(AnnotationsUiEvent.OnRemoveCancel) },
        isDestructive = true,
        icon = Icons.Default.Delete,
    )
}
