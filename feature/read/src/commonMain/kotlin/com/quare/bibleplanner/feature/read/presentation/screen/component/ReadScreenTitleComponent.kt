package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory

@Composable
internal fun ReadScreenTitleComponent(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    bookName: String,
    chapterNumber: Int,
    style: TextStyle = LocalTextStyle.current,
) {
    Row(
        modifier = modifier,
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
            style = style,
        )
        Text(
            text = "$chapterNumber",
            modifier = SharedTransitionModifierFactory.getReadTopBarSharedTransitionBookChapterModifier(
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                chapterNumber = chapterNumber,
                bookName = bookName,
            ),
            style = style,
        )
    }
}
