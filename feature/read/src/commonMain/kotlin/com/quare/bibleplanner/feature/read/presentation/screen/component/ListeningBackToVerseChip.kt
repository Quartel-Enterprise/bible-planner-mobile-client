package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_back_to_verse
import org.jetbrains.compose.resources.stringResource

internal const val LISTENING_BACK_TO_VERSE_TAG = "listening_back_to_verse"
private val chipHeight = 40.dp

@Composable
internal fun ListeningBackToVerseChip(
    verseLabel: String,
    isVerseAbove: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier.height(chipHeight),
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = if (isVerseAbove) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = null,
            )
            Text(
                text = stringResource(Res.string.listening_back_to_verse, verseLabel),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
