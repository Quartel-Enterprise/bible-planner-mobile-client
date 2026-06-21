package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MoreMenuItem(
    itemModel: MoreMenuItemPresentationModel,
    subtitle: String? = null,
    isSubtitleLoading: Boolean = false,
    onClick: () -> Unit,
    iconColor: Color = LocalContentColor.current.copy(alpha = 0.5f),
    trailingContent: @Composable (() -> Unit)? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    sharedElementKey: String? = null,
    sharedElementSubtitleKey: String? = null,
) {
    val text = stringResource(itemModel.name)
    val finalSubtitle = subtitle ?: itemModel.subtitle?.let { stringResource(it) }

    ListItem(
        headlineContent = {
            if (sharedTransitionScope != null && animatedContentScope != null && sharedElementKey != null) {
                with(sharedTransitionScope) {
                    Text(
                        text = text,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = sharedElementKey),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    )
                }
            } else {
                Text(text = text)
            }
        },
        supportingContent = when {
            isSubtitleLoading -> {
                { ShimmerBox(modifier = Modifier.width(120.dp).height(14.dp)) }
            }

            finalSubtitle != null -> {
                {
                    if (sharedTransitionScope != null && animatedContentScope != null &&
                        sharedElementSubtitleKey != null
                    ) {
                        with(sharedTransitionScope) {
                            Text(
                                text = finalSubtitle,
                                modifier = Modifier.sharedElement(
                                    rememberSharedContentState(key = sharedElementSubtitleKey),
                                    animatedVisibilityScope = animatedContentScope,
                                ),
                            )
                        }
                    } else {
                        Text(finalSubtitle)
                    }
                }
            }

            else -> {
                null
            }
        },
        leadingContent = {
            MoreItemIcon(
                icon = itemModel.icon,
                contentDescription = text,
                iconColor = iconColor,
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
