package com.quare.bibleplanner.android.feature.pixqr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.feature.donation.pixqr.presentation.PixQrUiEvent

@Composable
internal fun PixQrContent(onEvent: (PixQrUiEvent) -> Unit) {
    AppAlertDialog(
        title = stringResource(R.string.pix_qr_title),
        confirmText = stringResource(R.string.pix_qr_close),
        onConfirm = { onEvent(PixQrUiEvent.Dismiss) },
        dismissText = null,
        onDismiss = { onEvent(PixQrUiEvent.Dismiss) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Why: banking apps read dark modules on a light background best, so the code stays black on white in dark theme too.
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = MaterialTheme.shapes.large,
                    ).padding(12.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.pix_qr_code),
                    contentDescription = stringResource(R.string.pix_qr_description),
                    modifier = Modifier.size(232.dp),
                    tint = Color.Black,
                )
            }
            Text(
                text = stringResource(R.string.pix_qr_instruction),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
