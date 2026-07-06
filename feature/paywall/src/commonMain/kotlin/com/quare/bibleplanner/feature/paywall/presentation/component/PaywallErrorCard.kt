package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_error_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaywallErrorCard(modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.colorScheme.error
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = errorColor,
        ),
    ) {
        Text(
            text = stringResource(Res.string.paywall_error_message),
            modifier = Modifier.padding(16.dp),
            color = errorColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
