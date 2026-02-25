package com.quare.bibleplanner.feature.read.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadTopBar(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    titleStringResource: StringResource,
    chapterNumber: Int,
    isScrolled: Boolean,
    onBackClick: () -> Unit,
) {
    Surface(
        shadowElevation = if (isScrolled) 4.dp else 0.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        val bookName = stringResource(titleStringResource)
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
                    onBackClick = onBackClick,
                )
            },
        )
    }
}
