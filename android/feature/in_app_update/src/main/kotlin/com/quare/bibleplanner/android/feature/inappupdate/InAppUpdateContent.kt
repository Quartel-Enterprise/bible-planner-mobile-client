package com.quare.bibleplanner.android.feature.inappupdate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.icon.SheetIconTile
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiState
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private val bodyMaxWidth = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InAppUpdateContent(
    uiState: InAppUpdateUiState,
    onEvent: (InAppUpdateUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    val onDismiss = { sheetCloseGuard.close { onEvent(InAppUpdateUiEvent.OnDismiss) } }
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp,
                ).verticalScroll(rememberScrollState()),
        ) {
            SheetIconTile(imageVector = Icons.Rounded.SystemUpdate)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.in_app_update_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            uiState.versionName?.let { versionName ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.in_app_update_version, versionName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier.widthIn(max = bodyMaxWidth),
                text = stringResource(R.string.in_app_update_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Why: the ViewModel pops the sheet once the update starts, so a second tap must not pop the screen below.
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { sheetCloseGuard.close { onEvent(InAppUpdateUiEvent.OnUpdateClick) } },
            ) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = Icons.Rounded.SystemUpdate,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.in_app_update_action))
            }
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.in_app_update_dismiss))
            }
        }
    }
}
