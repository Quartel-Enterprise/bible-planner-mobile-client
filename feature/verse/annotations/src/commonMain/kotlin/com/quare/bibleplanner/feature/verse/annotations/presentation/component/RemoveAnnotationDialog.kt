package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.cancel
import bibleplanner.feature.verse.annotations.generated.resources.remove
import bibleplanner.feature.verse.annotations.generated.resources.remove_body
import bibleplanner.feature.verse.annotations.generated.resources.remove_title
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationItemUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RemoveAnnotationDialog(
    item: AnnotationItemUiModel,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    val passage = item.passage
    AppAlertDialog(
        title = stringResource(
            Res.string.remove_title,
            verseReferenceLabel(
                bookId = passage.chapter.bookId,
                chapterNumber = passage.chapter.chapterNumber,
                verseNumbers = passage.verseNumbers,
            ),
        ),
        text = stringResource(Res.string.remove_body),
        confirmText = stringResource(Res.string.remove),
        onConfirm = { onEvent(AnnotationsUiEvent.OnRemoveConfirm(item)) },
        dismissText = stringResource(Res.string.cancel),
        onDismiss = { onEvent(AnnotationsUiEvent.OnRemoveCancel) },
        isDestructive = true,
        icon = Icons.Default.Delete,
    )
}
