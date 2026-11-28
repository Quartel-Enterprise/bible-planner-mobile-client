package com.quare.bibleplanner.android.feature.materialyou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.feature.materialyou.presentation.model.AndroidColorSchemeUiEvent

@Composable
internal fun MaterialYouContent(
    isDynamicColorsEnabled: Boolean,
    onEvent: (AndroidColorSchemeUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(R.string.material_you_title),
        confirmText = stringResource(R.string.material_you_got_it),
        onConfirm = { onEvent(AndroidColorSchemeUiEvent.BottomSheetGotItClick) },
        dismissText = null,
        onDismiss = { onEvent(AndroidColorSchemeUiEvent.OnInformationDialogDismiss) },
        icon = Icons.Outlined.Palette,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isDynamicColorsEnabled,
                        role = Role.Switch,
                        onValueChange = { onEvent(AndroidColorSchemeUiEvent.OnIsDynamicColorsEnabledChange(it)) },
                    ).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.material_you_toggle_label),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = isDynamicColorsEnabled,
                    onCheckedChange = null,
                )
            }
            Text(text = stringResource(R.string.material_you_message))
            Text(
                text = stringResource(R.string.material_you_compat_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
