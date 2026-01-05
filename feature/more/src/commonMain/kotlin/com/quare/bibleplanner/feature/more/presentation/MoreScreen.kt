package com.quare.bibleplanner.feature.more.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_premium
import bibleplanner.feature.more.generated.resources.become_premium_subtitle
import bibleplanner.feature.more.generated.resources.data_section
import bibleplanner.feature.more.generated.resources.donate_option
import bibleplanner.feature.more.generated.resources.donate_subtitle
import bibleplanner.feature.more.generated.resources.legal_section
import bibleplanner.feature.more.generated.resources.preferences
import bibleplanner.feature.more.generated.resources.social_section
import bibleplanner.feature.more.generated.resources.today
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreIcon
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.utils.toStringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreScreen(
    state: MoreUiState,
    onEvent: (MoreUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.isFreeUser || state.shouldShowDonateOption) {
            state.headerRes?.let { headerRes ->
                item { SectionHeader(stringResource(headerRes)) }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.isFreeUser) {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(Res.string.become_premium),
                            subtitle = stringResource(Res.string.become_premium_subtitle),
                            icon = Icons.Default.Star,
                            onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.BECOME_PREMIUM)) },
                        )
                    }
                    if (state.shouldShowDonateOption) {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(Res.string.donate_option),
                            subtitle = stringResource(Res.string.donate_subtitle),
                            icon = Icons.Default.Favorite,
                            onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.DONATE)) },
                        )
                    }
                }
            }
        }

        item { SectionHeader(stringResource(Res.string.preferences)) }
        item {
            SectionCard {
                MoreMenuItem(
                    itemModel = MoreMenuOptionsFactory.theme,
                    subtitle = stringResource(state.themeSubtitle),
                    onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.THEME)) },
                )
                HorizontalDivider()
                val planStartDateSubtitle = state.planStartDate
                    ?.let { date ->
                        val isToday = date == state.currentDate
                        val prefix = if (isToday) "${stringResource(Res.string.today)}, " else ""
                        val month = stringResource(date.month.toStringResource()).take(3)
                        "$prefix${date.day} $month ${date.year}"
                    }.orEmpty()
                MoreMenuItem(
                    itemModel = MoreMenuOptionsFactory.editStartDate,
                    subtitle = planStartDateSubtitle,
                    onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.EDIT_PLAN_START_DAY)) },
                )
            }
        }

        item { SectionHeader(stringResource(Res.string.data_section)) }
        item {
            SectionCard {
                MoreMenuItem(
                    itemModel = MoreMenuOptionsFactory.deleteProgress,
                    onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.DELETE_PROGRESS)) },
                    isDestructive = true,
                )
            }
        }

        item { SectionHeader(stringResource(Res.string.legal_section)) }
        item {
            SectionCard {
                MoreMenuItem(
                    itemModel = MoreMenuOptionsFactory.privacyPolicy,
                    onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.PRIVACY_POLICY)) },
                )
                HorizontalDivider()
                MoreMenuItem(
                    itemModel = MoreMenuOptionsFactory.termsOfService,
                    onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.TERMS)) },
                )
            }
        }

        if (state.isInstagramLinkVisible) {
            item { SectionHeader(stringResource(Res.string.social_section)) }
            item {
                SectionCard {
                    MoreMenuItem(
                        itemModel = MoreMenuOptionsFactory.instagram,
                        onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.INSTAGRAM)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MoreMenuItem(
    itemModel: MoreMenuItemPresentationModel,
    subtitle: String? = null,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val text = stringResource(itemModel.name)
    ListItem(
        headlineContent = {
            Text(text = text)
        },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            MoreItemIcon(
                icon = itemModel.icon,
                contentDescription = text,
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun MoreItemIcon(
    icon: MoreIcon,
    contentDescription: String?,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    when (icon) {
        is MoreIcon.DrawableResourceIcon -> {
            Icon(
                painter = painterResource(icon.resource),
                contentDescription = contentDescription,
                tint = tint,
            )
        }

        is MoreIcon.ImageVectorIcon -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                tint = tint,
            )
        }
    }
}
