package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_dismiss
import bibleplanner.feature.books.generated.resources.content_description_info
import bibleplanner.feature.books.generated.resources.reading_not_available_yet
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BooksInformationBox(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(Res.string.content_description_info),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            HorizontalSpacer(16.dp)

            Text(
                text = stringResource(Res.string.reading_not_available_yet),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            HorizontalSpacer(12.dp)

            CommonIconButton(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.content_description_dismiss),
                onClick = onDismiss,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
