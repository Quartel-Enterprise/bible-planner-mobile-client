package com.quare.bibleplanner.ui.component.icon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.favorite
import org.jetbrains.compose.resources.stringResource

/**
 * A reusable favorite icon that supports shared element transitions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoriteIcon(
    isFavorite: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedElementKey: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = stringResource(Res.string.favorite),
    unselectedTint: Color? = null,
) {
    val iconTint = if (isFavorite) {
        MaterialTheme.colorScheme.error
    } else {
        unselectedTint ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    with(sharedTransitionScope) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = modifier.sharedElement(
                rememberSharedContentState(key = sharedElementKey),
                animatedVisibilityScope = animatedVisibilityScope,
            ),
        )
    }
}

/**
 * A reusable favorite icon button that supports shared element transitions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedElementKey: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = stringResource(Res.string.favorite),
    unselectedTint: Color? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        FavoriteIcon(
            isFavorite = isFavorite,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            sharedElementKey = sharedElementKey,
            contentDescription = contentDescription,
            unselectedTint = unselectedTint,
        )
    }
}
