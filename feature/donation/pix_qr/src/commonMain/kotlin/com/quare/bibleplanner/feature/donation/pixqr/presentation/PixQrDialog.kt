package com.quare.bibleplanner.feature.donation.pixqr.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.donation.pix_qr.generated.resources.Res
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_close
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_description
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_instruction
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_share
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_title
import bibleplanner.feature.donation.pix_qr.generated.resources.qr_code_pix
import com.quare.bibleplanner.ui.component.icon.ShareIconButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PixQrDialog(onEvent: (PixQrUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(PixQrUiEvent.Dismiss) },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.pix_qr_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                ShareIconButton(
                    onClick = { onEvent(PixQrUiEvent.Share) },
                    contentDescription = stringResource(Res.string.pix_qr_share),
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.qr_code_pix),
                    contentDescription = stringResource(Res.string.pix_qr_description),
                    modifier = Modifier.size(250.dp),
                    tint = MaterialTheme.colorScheme.inverseSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.pix_qr_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onEvent(PixQrUiEvent.Dismiss) }) {
                Text(stringResource(Res.string.pix_qr_close))
            }
        },
    )
}
