package com.quare.bibleplanner.android.feature.contactsupport

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiAction
import com.quare.bibleplanner.feature.contactsupport.presentation.viewmodel.ContactSupportViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import org.koin.compose.viewmodel.koinViewModel

internal fun ContactSupportNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<ContactSupportViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val snackbarHostState = remember { SnackbarHostState() }
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is ContactSupportUiAction.OpenLink -> context.openEmailApp(uiAction.url)

            is ContactSupportUiAction.Copy -> clipboard.setClipEntry(uiAction.text.toClipEntry())

            /*
             * Why: from Android 13 the system confirms every copy itself, and Android asks apps not to
             * show a second confirmation on top of it.
             */
            ContactSupportUiAction.NotifyEmailCopied -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                snackbarHostState.showSnackbar(context.getString(R.string.contact_support_email_copied))
            }
        }
    }
    ExpressiveTheme {
        ContactSupportContent(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun Context.openEmailApp(mailto: String) {
    try {
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(mailto)))
    } catch (_: ActivityNotFoundException) {
        // Why: with no email app the address stays on screen with its copy button.
    } catch (_: SecurityException) {
        // Why: a work profile policy or a non-exported mail activity can refuse the launch, like no email app.
    }
}
