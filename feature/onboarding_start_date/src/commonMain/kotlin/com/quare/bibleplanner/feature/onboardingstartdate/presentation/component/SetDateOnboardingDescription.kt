package com.quare.bibleplanner.feature.onboardingstartdate.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.onboarding_start_date.generated.resources.Res
import bibleplanner.feature.onboarding_start_date.generated.resources.onboarding_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetDateOnboardingDescription(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(Res.string.onboarding_description),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
