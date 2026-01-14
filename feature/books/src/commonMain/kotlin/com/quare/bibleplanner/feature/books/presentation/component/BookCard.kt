package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.utils.getShadowClip

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookCard(
    book: BookPresentationModel,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    with(sharedTransitionScope) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .sharedBounds(
                    rememberSharedContentState(key = "book-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    clipInOverlayDuringTransition = getShadowClip(RoundedCornerShape(16.dp)),
                ),
            shape = RoundedCornerShape(16.dp),
            content = content,
        )
    }
}
