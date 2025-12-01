package com.quare.bibleplanner.feature.materialyou.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import bibleplanner.feature.material_you.generated.resources.Res
import bibleplanner.feature.material_you.generated.resources.dynamic_colors_compat_note
import bibleplanner.feature.material_you.generated.resources.dynamic_colors_message
import bibleplanner.feature.material_you.generated.resources.dynamic_colors_title
import bibleplanner.feature.material_you.generated.resources.dynamic_colors_toggle_label
import bibleplanner.feature.material_you.generated.resources.got_it
import com.quare.bibleplanner.feature.materialyou.presentation.model.AndroidColorSchemeUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun MaterialYouDialog(
    isMaterialYouActivated: Boolean,
    onEvent: (AndroidColorSchemeUiEvent) -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onEvent(AndroidColorSchemeUiEvent.OnInformationDialogDismiss)
        },
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = stringResource(Res.string.dynamic_colors_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.dynamic_colors_toggle_label),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = isMaterialYouActivated,
                        onCheckedChange = {
                            onEvent(AndroidColorSchemeUiEvent.OnIsDynamicColorsEnabledChange(it))
                        },
                    )
                }

                Text(
                    text = stringResource(Res.string.dynamic_colors_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(Res.string.dynamic_colors_compat_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )

                VerticalSpacer(size = 4)

                Button(
                    onClick = {
                        onEvent(AndroidColorSchemeUiEvent.BottomSheetGotItClick)
                    },
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(text = stringResource(Res.string.got_it))
                }
            }
        }
    }
}
