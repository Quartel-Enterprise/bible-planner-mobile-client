package com.quare.bibleplanner.feature.donation.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.donation.generated.resources.Res
import bibleplanner.feature.donation.generated.resources.donation_address_copied
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DonationItem(
    title: String,
    description: String,
    icon: Painter,
    iconColor: Color,
    trailingIcon: Painter? = null,
    isExpanded: Boolean = false,
    isCopied: Boolean = false,
    onClick: () -> Unit,
    expandedContent: (@Composable () -> Unit)? = null,
) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)
    val borderColor by animateColorAsState(
        targetValue = if (isCopied) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor,
        ),
    ) {
        Column {
            ListItem(
                headlineContent = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supportingContent = if (isCopied) {
                    {
                        Text(
                            text = stringResource(Res.string.donation_address_copied),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                leadingContent = {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        color = iconColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp),
                            tint = iconColor,
                        )
                    }
                },
                trailingContent = {
                    if (expandedContent != null) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation),
                        )
                    } else if (isCopied) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    } else if (trailingIcon != null) {
                        Icon(
                            painter = trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )

            if (expandedContent != null) {
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                        expandedContent()
                    }
                }
            }
        }
    }
}

@Composable
internal fun SubDonationItem(
    title: String,
    icon: Painter?,
    description: String? = null,
    isCopied: Boolean = false,
    onCopy: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isCopied) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isCopied) {
            MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.3f,
            )
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor,
        ),
        color = backgroundColor,
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            supportingContent = if (isCopied) {
                {
                    Text(
                        text = stringResource(Res.string.donation_address_copied),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                description?.let {
                    {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            leadingContent = icon?.let {
                {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            trailingContent = {
                if (isCopied) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            modifier = Modifier.clickable(onClick = onCopy),
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
