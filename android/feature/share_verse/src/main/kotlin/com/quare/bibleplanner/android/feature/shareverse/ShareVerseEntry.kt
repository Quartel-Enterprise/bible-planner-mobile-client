package com.quare.bibleplanner.android.feature.shareverse

import android.content.ActivityNotFoundException
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.core.utils.shareContent
import com.quare.bibleplanner.feature.verse.share.presentation.ShareVerseViewModel
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.utils.rememberImageSaver
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun ShareVerseNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<ShareVerseViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    /*
     * Why: a second tap before the share sheet shows up would open it twice. The guard lifts when the user comes back
     * to the app, or at once when the launch fails, so it never depends on how fast the share sheet starts.
     */
    var isSharingText by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { isSharingText = false }
    ActionCollector(viewModel.uiAction) { uiAction ->
        if (uiAction is ShareVerseUiAction.ShareText) {
            isSharingText = launchShare(message = uiAction.content.shareText)
        }
    }
    ExpressiveTheme {
        ShareVerseContent(
            uiState = uiState,
            onEvent = { event ->
                if (event == ShareVerseUiEvent.OnShareAsTextClick) {
                    if (!isSharingText) {
                        isSharingText = true
                        viewModel.onEvent(event)
                    }
                } else {
                    viewModel.onEvent(event)
                }
            },
        )
    }
}

internal fun ShareVerseImageNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<ShareVerseViewModel> {
        parametersOf(
            ShareVerseNavRoute(
                bookId = bookId,
                chapterNumber = chapterNumber,
                verseNumbers = verseNumbers,
            ),
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val actionGuard = remember { ShareImageActionGuard() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { actionGuard.isSharing = false }
    ShareVerseImageActionCollector(
        viewModel = viewModel,
        actionGuard = actionGuard,
        snackbarHostState = snackbarHostState,
    )
    ExpressiveTheme {
        ShareVerseImageContent(
            uiState = uiState,
            isWideLayout = LocalIsWideLayout.current,
            actionGuard = actionGuard,
            snackbarHostState = snackbarHostState,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun ShareVerseImageActionCollector(
    viewModel: ShareVerseViewModel,
    actionGuard: ShareImageActionGuard,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val showMessage: (Int) -> Unit = { messageRes ->
        coroutineScope.launch { snackbarHostState.showSnackbar(context.getString(messageRes)) }
    }
    // Why: a second picker launched while the first is open would answer only one of them.
    var isPickingPhoto by remember { mutableStateOf(false) }
    val photoPicker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        isPickingPhoto = false
        viewModel.onEvent(ShareVerseUiEvent.OnPhotoPicked(file))
    }
    val saveImage = rememberImageSaver { result ->
        actionGuard.isSaving = false
        result.toMessageRes()?.let(showMessage)
    }
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is ShareVerseUiAction.ShareText -> launchShare(message = uiAction.content.shareText)

            is ShareVerseUiAction.ShareImage -> {
                actionGuard.isSharing = launchShare(
                    message = uiAction.content.shareText,
                    imageBytes = uiAction.imageBytes,
                )
            }

            is ShareVerseUiAction.SaveImage -> saveImage(uiAction.imageBytes, uiAction.fileName)

            ShareVerseUiAction.LaunchPhotoPicker -> if (!isPickingPhoto) {
                isPickingPhoto = true
                photoPicker.launch()
            }

            ShareVerseUiAction.NotifyPhotoLoadFailed -> showMessage(R.string.share_verse_photo_load_failed)
        }
    }
}

private fun launchShare(
    message: String,
    imageBytes: ByteArray? = null,
): Boolean = try {
    shareContent(
        message = message,
        imageBytes = imageBytes,
    )
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}

@StringRes
private fun ImageSaveResult.toMessageRes(): Int? = when (this) {
    ImageSaveResult.SAVED -> R.string.share_verse_image_saved
    ImageSaveResult.FAILED -> R.string.share_verse_image_save_failed
    ImageSaveResult.CANCELLED -> null
}
