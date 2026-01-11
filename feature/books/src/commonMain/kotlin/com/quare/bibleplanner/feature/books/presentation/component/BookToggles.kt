package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent

@Composable
internal fun BookToggles(
    selectedTestament: BookTestament,
    selectedLayoutFormat: BookLayoutFormat,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1.55f)) {
            TestamentToggle(
                selectedTestament = selectedTestament,
                onEvent = onEvent,
            )
        }
        Box(modifier = Modifier.weight(0.45f)) {
            LayoutToggle(
                selectedLayoutFormat = selectedLayoutFormat,
                onEvent = onEvent,
            )
        }
    }
}
