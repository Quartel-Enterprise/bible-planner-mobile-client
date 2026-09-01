package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.next_chapter
import bibleplanner.feature.read.generated.resources.previous_chapter
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import org.jetbrains.compose.resources.stringResource

private val buttonSize = 44.dp

/**
 * Keeps its slot when there is no suggestion so the read pill between the two arrows does not shift
 * on the first or last chapter of the plan.
 */
@Composable
internal fun ChapterNavigationButton(
    suggestion: ReadNavigationSuggestionModel?,
    isNext: Boolean,
    onClick: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestion == null) {
        Spacer(modifier = modifier.size(buttonSize))
        return
    }
    OutlinedIconButton(
        modifier = modifier.size(buttonSize),
        onClick = { onClick(ReadUiEvent.OnNavigationSuggestionClick(suggestion)) },
    ) {
        Icon(
            imageVector = if (isNext) {
                Icons.AutoMirrored.Filled.ArrowForward
            } else {
                Icons.AutoMirrored.Filled.ArrowBack
            },
            contentDescription = stringResource(
                if (isNext) Res.string.next_chapter else Res.string.previous_chapter,
            ),
        )
    }
}
