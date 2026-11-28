package com.quare.bibleplanner.android.feature.verseselection

import android.content.Context
import android.os.Build
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.model.route.getVerseSelectionPane
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.VerseSelectionViewModel
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import com.quare.bibleplanner.ui.utils.toClipEntry
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val instantTransition: ContentTransform = EnterTransition.None togetherWith ExitTransition.None

/*
 * Why: NavDisplay keys transitions on scene class, so reader -> reader+panel would run the default cross-fade, and the
 * reader vanishes meanwhile. The swap is instant and the sheet animates itself.
 */
internal fun VerseSelectionNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getVerseSelectionPane() +
        NavDisplay.transitionSpec { instantTransition } +
        NavDisplay.popTransitionSpec { instantTransition } +
        NavDisplay.predictivePopTransitionSpec { instantTransition },
) {
    val viewModel = koinViewModel<VerseSelectionViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is VerseSelectionUiAction.CopyToClipboard -> clipboard.setClipEntry(uiAction.text.toClipEntry())

            /*
             * Why: from Android 13 the system confirms every copy itself, and Android asks apps not to show a second
             * confirmation on top of it.
             */
            VerseSelectionUiAction.NotifyCopied -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                appSnackbarController.show(context.createCopiedMessage())
            }
        }
    }
    uiState?.let { safeUiState ->
        ExpressiveTheme {
            VerseSelectionContent(
                uiState = safeUiState,
                isWideLayout = LocalIsWideLayout.current,
                onEvent = viewModel::onEvent,
            )
        }
    }
}

private fun Context.createCopiedMessage(): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(R.string.verse_selection_copied_to_clipboard),
    isDismissible = true,
)
