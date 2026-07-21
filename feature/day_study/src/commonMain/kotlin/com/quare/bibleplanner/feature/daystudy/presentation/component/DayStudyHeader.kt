package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_title
import org.jetbrains.compose.resources.stringResource

private val iconSize = 22.dp
private val iconSpacing = 8.dp
private val passageLabelStartPadding = iconSize + iconSpacing

@Composable
internal fun DayStudyHeader(
    passageLabel: String?,
    showIcon: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(iconSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = stringResource(Res.string.ai_study_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        passageLabel?.let { label ->
            Text(
                text = label,
                modifier = Modifier.padding(
                    start = if (showIcon) passageLabelStartPadding else 0.dp,
                    top = 3.dp,
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
