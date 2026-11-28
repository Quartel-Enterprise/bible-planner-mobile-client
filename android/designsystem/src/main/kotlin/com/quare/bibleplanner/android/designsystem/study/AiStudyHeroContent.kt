package com.quare.bibleplanner.android.designsystem.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.R

private val descriptionMaxWidth = 300.dp
private val heroButtonHeight = 50.dp
private const val ACCENT_CONTAINER_ALPHA = 0.12f

/*
 * Why: the day and chapter studies share one empty state (icon, title, badge, hint and one action), so both
 * native screens draw it from here as their shared Compose Multiplatform versions did from ui/component.
 */
@Composable
fun AiStudyHeroContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonIcon: ImageVector,
    buttonLabel: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: @Composable () -> Unit = {},
) {
    AiStudyCenteredColumn(modifier = modifier) {
        AiStudyHeroIcon(icon = icon)
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            badge()
        }
        Spacer(modifier = Modifier.height(10.dp))
        AiStudyDescriptionText(text = subtitle)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.height(heroButtonHeight),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = buttonLabel)
            }
        }
    }
}

@Composable
fun AiStudyErrorContent(
    isOffline: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AiStudyHeroContent(
        icon = Icons.Rounded.CloudOff,
        title = stringResource(R.string.study_error_title),
        subtitle = stringResource(if (isOffline) R.string.study_error_offline else R.string.study_error),
        buttonIcon = Icons.Rounded.Refresh,
        buttonLabel = stringResource(R.string.study_retry),
        isLoading = false,
        onClick = onRetryClick,
        modifier = modifier,
    )
}

@Composable
fun AiStudyLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun AiStudyBadge(
    text: String,
    isAccent: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = if (isAccent) {
            MaterialTheme.colorScheme.primary.copy(alpha = ACCENT_CONTAINER_ALPHA)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (isAccent) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 2.dp,
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun AiStudyHeroIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = iconModifier.size(34.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun AiStudyCenteredColumn(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 38.dp,
                vertical = 40.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}

@Composable
private fun AiStudyDescriptionText(text: String) {
    Text(
        text = text,
        modifier = Modifier.widthIn(max = descriptionMaxWidth),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
