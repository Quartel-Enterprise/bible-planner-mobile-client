package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TestamentToggle(
    selectedTestament: BookTestament,
    onEvent: (BooksUiEvent) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val testaments = BookTestament.entries
        testaments.forEachIndexed { index, testament ->
            SegmentedButton(
                selected = selectedTestament == testament,
                onClick = { onEvent(BooksUiEvent.OnTestamentSelect(testament)) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = testaments.size,
                ),
                modifier = Modifier.height(36.dp),
            ) {
                Text(
                    text = stringResource(testament.titleRes),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                )
            }
        }
    }
}
