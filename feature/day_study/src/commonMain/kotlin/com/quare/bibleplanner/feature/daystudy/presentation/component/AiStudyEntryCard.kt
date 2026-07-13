package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_exhausted_subtitle
import bibleplanner.feature.day_study.generated.resources.ai_study_generate
import bibleplanner.feature.day_study.generated.resources.ai_study_generate_hint
import bibleplanner.feature.day_study.generated.resources.ai_study_generate_hint_pro
import bibleplanner.feature.day_study.generated.resources.ai_study_generated
import bibleplanner.feature.day_study.generated.resources.ai_study_generating_card_title
import bibleplanner.feature.day_study.generated.resources.ai_study_pro_badge
import bibleplanner.feature.day_study.generated.resources.ai_study_quota_free
import bibleplanner.feature.day_study.generated.resources.ai_study_subscribe
import bibleplanner.feature.day_study.generated.resources.ai_study_title
import bibleplanner.feature.day_study.generated.resources.ai_study_view
import bibleplanner.feature.day_study.generated.resources.ai_study_view_hint
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private const val SOFT_PRIMARY_ALPHA = 0.12f
private const val BORDER_ALPHA = 0.2f
private const val SKELETON_ALPHA = 0.18f
private const val SKELETON_TITLE_WIDTH_FRACTION = 0.5f
private const val SKELETON_SUBTITLE_WIDTH_FRACTION = 0.8f

@Composable
internal fun AiStudyEntryCard(
    card: DayStudyCardUiModel,
    generation: DayStudyGenerationUiModel?,
    isOpening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLocked = card.mode == DayStudyCardMode.LOCKED && generation == null
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 1.dp else 2.dp,
        ),
        border = if (isLocked) {
            null
        } else {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = BORDER_ALPHA),
            )
        },
    ) {
        if (generation != null) {
            GeneratingCardContent(
                generation = generation,
                onViewClick = onClick,
            )
        } else {
            IdleCardContent(
                card = card,
                isLocked = isLocked,
                isOpening = isOpening,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun IdleCardContent(
    card: DayStudyCardUiModel,
    isLocked: Boolean,
    isOpening: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CardIcon(
            icon = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
            isLocked = isLocked,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.ai_study_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                CardBadge(card)
            }
            Text(
                text = dayStudyCardSubtitle(card),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CardButton(
            mode = card.mode,
            isLoading = isOpening,
            onClick = onClick,
        )
    }
}

@Composable
private fun GeneratingCardContent(
    generation: DayStudyGenerationUiModel,
    onViewClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CardIcon(
            icon = Icons.Rounded.AutoAwesome,
            isLocked = false,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(Res.string.ai_study_generating_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(generation.activePhase.titleRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalSpacer(7)
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA),
                strokeCap = StrokeCap.Round,
            )
        }
        TextButton(onClick = onViewClick) {
            Text(text = stringResource(Res.string.ai_study_view))
        }
    }
}

@Composable
internal fun AiStudyEntryCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = BORDER_ALPHA),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(13.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(SKELETON_TITLE_WIDTH_FRACTION)
                        .height(16.dp),
                )
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(SKELETON_SUBTITLE_WIDTH_FRACTION)
                        .height(11.dp),
                )
            }
            SkeletonBox(
                modifier = Modifier
                    .width(72.dp)
                    .height(36.dp),
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun SkeletonBox(
    modifier: Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(6.dp),
) {
    ShimmerBox(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = SKELETON_ALPHA),
    )
}

@Composable
private fun CardIcon(
    icon: ImageVector,
    isLocked: Boolean,
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .background(
                color = if (isLocked) {
                    MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(13.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
internal fun CardBadge(card: DayStudyCardUiModel) {
    val isGenerated = card.mode == DayStudyCardMode.VIEW
    val isProBadge = card.isPro || card.mode == DayStudyCardMode.LOCKED
    val text = when {
        isGenerated -> stringResource(Res.string.ai_study_generated)
        isProBadge -> stringResource(Res.string.ai_study_pro_badge)
        card.remainingFree > 0 -> stringResource(Res.string.ai_study_quota_free, card.remainingFree)
        else -> return
    }
    val useAccentStyle = isGenerated || isProBadge
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (useAccentStyle) {
            MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (useAccentStyle) {
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
private fun CardButton(
    mode: DayStudyCardMode,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(
                    when (mode) {
                        DayStudyCardMode.GENERATE -> Res.string.ai_study_generate
                        DayStudyCardMode.VIEW -> Res.string.ai_study_view
                        DayStudyCardMode.LOCKED -> Res.string.ai_study_subscribe
                    },
                ),
            )
        }
    }
}

@Composable
internal fun dayStudyCardSubtitle(card: DayStudyCardUiModel): String = when (card.mode) {
    DayStudyCardMode.GENERATE -> if (card.isPro) {
        stringResource(Res.string.ai_study_generate_hint_pro)
    } else {
        stringResource(Res.string.ai_study_generate_hint)
    }

    DayStudyCardMode.VIEW -> stringResource(Res.string.ai_study_view_hint)

    DayStudyCardMode.LOCKED -> stringResource(
        Res.string.ai_study_exhausted_subtitle,
        card.freeLimit,
    )
}
