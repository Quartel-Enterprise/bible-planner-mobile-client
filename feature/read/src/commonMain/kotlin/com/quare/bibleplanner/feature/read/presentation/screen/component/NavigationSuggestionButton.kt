package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel

@Composable
internal fun NavigationSuggestionButton(
    modifier: Modifier = Modifier,
    suggestion: ReadNavigationSuggestionModel,
    isNext: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        val text = "${suggestion.bookId.getBookName()} ${suggestion.chapterNumber}"
        val icon = if (isNext) Icons.AutoMirrored.Filled.ArrowForwardIos else Icons.Default.ArrowBackIosNew
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isNext) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = icon,
                    contentDescription = text,
                )
            }
            Text(text = text)
            if (isNext) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = icon,
                    contentDescription = text,
                )
            }
        }
    }
}
