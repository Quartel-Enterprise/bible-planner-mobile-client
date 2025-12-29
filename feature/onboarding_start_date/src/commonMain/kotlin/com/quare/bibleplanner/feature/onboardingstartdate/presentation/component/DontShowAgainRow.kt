package com.quare.bibleplanner.feature.onboardingstartdate.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.onboarding_start_date.generated.resources.Res
import bibleplanner.feature.onboarding_start_date.generated.resources.dont_show_again
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DontShowAgainRow(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isDontShowAgainMarked: Boolean,
) {
    Row(
        modifier = modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Checkbox(
            checked = isDontShowAgainMarked,
            onCheckedChange = {
                onClick()
            },
        )
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(Res.string.dont_show_again),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
