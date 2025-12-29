package com.quare.bibleplanner.feature.onboardingstartdate.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.onboarding_start_date.generated.resources.Res
import bibleplanner.feature.onboarding_start_date.generated.resources.set_date
import bibleplanner.feature.onboarding_start_date.generated.resources.start_now
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.model.OnboardingStartDateUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ActionButtons(
    modifier: Modifier = Modifier,
    onEvent: (OnboardingStartDateUiEvent) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = {
                onEvent(OnboardingStartDateUiEvent.OnSetDateClick)
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.EditCalendar,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(stringResource(Res.string.set_date))
        }

        Button(
            onClick = {
                onEvent(OnboardingStartDateUiEvent.OnStartNowClick)
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(stringResource(Res.string.start_now))
        }
    }
}
