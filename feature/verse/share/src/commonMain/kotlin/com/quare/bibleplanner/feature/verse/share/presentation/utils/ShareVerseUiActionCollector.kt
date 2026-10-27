package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.quare.bibleplanner.core.utils.shareContent
import com.quare.bibleplanner.feature.verse.share.presentation.mapper.toMessage
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Composable
internal fun ShareVerseUiActionCollector(
    uiActionFlow: Flow<ShareVerseUiAction>,
    snackbarHostState: SnackbarHostState,
    onPhotoPicked: (PlatformFile?) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val showMessage: (StringResource) -> Unit = { stringResource ->
        coroutineScope.launch { snackbarHostState.showSnackbar(getString(stringResource)) }
    }
    val photoPicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = onPhotoPicked,
    )
    val saveImage = rememberImageSaver { result -> result.toMessage()?.let(showMessage) }
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            is ShareVerseUiAction.ShareText -> shareContent(uiAction.content.shareText)

            is ShareVerseUiAction.ShareImage -> {
                shareContent(
                    message = uiAction.content.shareText,
                    imageBytes = uiAction.imageBytes,
                )
            }

            is ShareVerseUiAction.SaveImage -> saveImage(uiAction.imageBytes, uiAction.fileName)

            ShareVerseUiAction.LaunchPhotoPicker -> photoPicker.launch()

            is ShareVerseUiAction.ShowMessage -> showMessage(uiAction.stringResource)
        }
    }
}
