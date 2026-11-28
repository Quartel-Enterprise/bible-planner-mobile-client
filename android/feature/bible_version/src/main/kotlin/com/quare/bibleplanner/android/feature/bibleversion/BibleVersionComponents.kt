package com.quare.bibleplanner.android.feature.bibleversion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val MAX_ABBREVIATION_LENGTH = 3
private const val BYTES_IN_MEGABYTE = 1_000_000
private const val DECIMAL_PLACE = 10

@Composable
internal fun BibleVersionAbbreviationChip(
    versionId: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = versionId.take(MAX_ABBREVIATION_LENGTH).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        )
    }
}

@Composable
internal fun megabytesText(bytes: Long): String = stringResource(
    R.string.bible_version_megabytes,
    bytes / BYTES_IN_MEGABYTE,
    bytes % BYTES_IN_MEGABYTE * DECIMAL_PLACE / BYTES_IN_MEGABYTE,
)
