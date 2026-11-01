package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_error_loading
import bibleplanner.feature.release_notes.generated.resources.release_notes_screen_title
import bibleplanner.feature.release_notes.generated.resources.release_notes_view_all_github
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.feature.releasenotes.presentation.model.VisibleReleaseNote
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model.ReleaseNotesNativeColors
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model.ReleaseNotesNativeContent
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model.ReleaseNotesNativeNote
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model.ReleaseNotesNativeState
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model.ReleaseNotesNativeTab
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model.ReleaseNotesNativeTag
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.isUpcoming
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.tagBackgroundColor
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.tagContentColor
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.toTagResource
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.toTitleResource
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.toVisibleNotes
import com.quare.bibleplanner.ui.utils.format
import org.jetbrains.compose.resources.stringResource

/*
 * Why: strings, dates and colors are resolved here, inside the composition, so the SwiftUI screen
 * follows the in-app language and theme exactly like the Compose screens do.
 */
@Composable
internal fun ReleaseNotesUiState.toNativeState(): ReleaseNotesNativeState = ReleaseNotesNativeState(
    title = stringResource(Res.string.release_notes_screen_title),
    githubButtonLabel = stringResource(Res.string.release_notes_view_all_github),
    content = toNativeContent(),
    colors = nativeColors(),
)

@Composable
private fun ReleaseNotesUiState.toNativeContent(): ReleaseNotesNativeContent = when (this) {
    ReleaseNotesUiState.Loading -> ReleaseNotesNativeContent.Loading

    ReleaseNotesUiState.Error -> ReleaseNotesNativeContent.Error(
        message = stringResource(Res.string.release_notes_error_loading),
    )

    is ReleaseNotesUiState.Success -> ReleaseNotesNativeContent.Success(
        tabs = availableTabs.map { tab ->
            ReleaseNotesNativeTab(
                tab = tab,
                title = stringResource(tab.toTitleResource()),
                isSelected = tab == currentTab,
            )
        },
        notes = toVisibleNotes().map { visibleNote -> visibleNote.toNativeNote() },
    )
}

@Composable
private fun VisibleReleaseNote.toNativeNote(): ReleaseNotesNativeNote = ReleaseNotesNativeNote(
    version = note.version,
    dateText = note.dateRepresentation?.format(),
    changes = note.changes,
    tag = type.toTagResource()?.let { tagResource ->
        ReleaseNotesNativeTag(
            text = stringResource(tagResource),
            backgroundArgb = type.tagBackgroundColor().toArgb(),
            contentArgb = type.tagContentColor().toArgb(),
        )
    },
    isUpcoming = type.isUpcoming(),
)

@Composable
private fun nativeColors(): ReleaseNotesNativeColors {
    val colorScheme = MaterialTheme.colorScheme
    return ReleaseNotesNativeColors(
        backgroundArgb = colorScheme.background.toArgb(),
        onBackgroundArgb = colorScheme.onBackground.toArgb(),
        cardArgb = colorScheme.surfaceContainerLow.toArgb(),
        onCardArgb = colorScheme.onSurface.toArgb(),
        onCardVariantArgb = colorScheme.onSurfaceVariant.toArgb(),
        primaryArgb = colorScheme.primary.toArgb(),
        errorArgb = colorScheme.error.toArgb(),
    )
}
