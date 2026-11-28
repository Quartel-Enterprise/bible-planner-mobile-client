package com.quare.bibleplanner.android.feature.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.titleRes
import org.jetbrains.compose.resources.stringResource as sharedStringResource

@Composable
internal fun BookGroupHeader(
    group: BookGroup,
    transitionScopes: BooksTransitionScopes?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = sharedStringResource(group.titleRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .padding(end = 8.dp)
                .semantics { heading() }
                .sharedElementOf(
                    transitionScopes = transitionScopes,
                    key = "book-group-$group",
                ),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        )
    }
}
