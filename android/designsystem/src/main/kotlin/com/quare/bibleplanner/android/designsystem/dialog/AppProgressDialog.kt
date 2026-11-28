package com.quare.bibleplanner.android.designsystem.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.quare.bibleplanner.android.designsystem.R

private val progressSpacing = 18.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppProgressDialog(message: String?) {
    HideSceneDialogScrimEffect()
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(progressSpacing),
            ) {
                val loadingDescription = stringResource(R.string.progress_dialog_loading)
                LoadingIndicator(
                    modifier = Modifier.semantics {
                        if (message == null) contentDescription = loadingDescription
                    },
                )
                message?.let { safeMessage ->
                    Text(
                        text = safeMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        confirmButton = {},
    )
}
