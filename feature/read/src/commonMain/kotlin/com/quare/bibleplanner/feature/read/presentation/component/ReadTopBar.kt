package com.quare.bibleplanner.feature.read.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.change_bible_version
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadTopBar(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isScrolled: Boolean,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    Surface(
        shadowElevation = if (isScrolled) 4.dp else 0.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        val bookName = stringResource(state.bookStringResource)
        val chapterNumber = state.chapterNumber
        TopAppBar(
            modifier = modifier,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        modifier = SharedTransitionModifierFactory.getBookNameSharedTransitionModifier(
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope,
                            bookName = bookName,
                        ),
                        text = bookName,
                    )
                    Text(
                        text = "$chapterNumber",
                        modifier = SharedTransitionModifierFactory.getReadTopBarSharedTransitionBookChapterModifier(
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope,
                            chapterNumber = chapterNumber,
                            bookName = bookName,
                        ),
                    )
                }
            },
            navigationIcon = {
                BackIcon(
                    onBackClick = {
                        onEvent(ReadUiEvent.OnArrowBackClick)
                    },
                )
            },
            actions = {
                CommonIconButton(
                    imageVector = Icons.Default.ChangeCircle,
                    onClick = {
                        onEvent(ReadUiEvent.ManageBibleVersions)
                    },
                    contentDescription = stringResource(Res.string.change_bible_version),
                )
            },
        )
    }
}
