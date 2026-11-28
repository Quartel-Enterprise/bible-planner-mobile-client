package com.quare.bibleplanner.android.feature.paywall

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.model.SubscriptionPlanPresentationModel

private const val ANIMATION_TIME_MILLIS = 350
private const val SELECTED_CONTAINER_ALPHA = 0.4f
private val planShape = RoundedCornerShape(12.dp)

@Composable
internal fun SubscriptionPlanList(
    subscriptionPlans: List<SubscriptionPlanPresentationModel>,
    onPlanSelected: (SubscriptionPlanType) -> Unit,
    itemSpacing: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        subscriptionPlans.forEach { plan ->
            Box {
                SubscriptionPlanCard(
                    modifier = Modifier.fillMaxWidth(),
                    isSelected = plan.isSelected,
                    onClick = { onPlanSelected(plan.type) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = plan.isSelected,
                            onClick = null,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            PlanTitleText(type = plan.type)
                            Spacer(modifier = Modifier.height(2.dp))
                            PlanDescriptionText(type = plan.type)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = plan.priceDescription,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                            )
                            Text(
                                text = stringResource(plan.type.toPeriod()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
                plan.savePercentage?.let { savePercentage ->
                    PlanDiscountBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(
                                x = (-16).dp,
                                y = (-10).dp,
                            ),
                        savePercentage = savePercentage,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SubscriptionPlanRow(
    subscriptionPlans: List<SubscriptionPlanPresentationModel>,
    onPlanSelected: (SubscriptionPlanType) -> Unit,
    itemSpacing: Dp,
    priceFontSize: TextUnit,
    priceUnitFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        subscriptionPlans.forEach { plan ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                SubscriptionPlanCard(
                    modifier = Modifier.fillMaxHeight(),
                    isSelected = plan.isSelected,
                    onClick = { onPlanSelected(plan.type) },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = plan.isSelected,
                                onClick = null,
                            )
                            PlanTitleText(type = plan.type)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        PlanPriceText(
                            price = plan.priceDescription,
                            periodUnit = stringResource(plan.type.toPeriodUnit()),
                            priceFontSize = priceFontSize,
                            priceUnitFontSize = priceUnitFontSize,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        PlanDescriptionText(type = plan.type)
                    }
                }
                plan.savePercentage?.let { savePercentage ->
                    PlanDiscountBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(
                                x = (-12).dp,
                                y = (-10).dp,
                            ),
                        savePercentage = savePercentage,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = SELECTED_CONTAINER_ALPHA)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = ANIMATION_TIME_MILLIS),
        label = "planContainerColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(durationMillis = ANIMATION_TIME_MILLIS),
        label = "planBorderColor",
    )
    Card(
        modifier = modifier
            .clip(planShape)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = planShape,
        border = BorderStroke(
            width = 2.dp,
            color = borderColor,
        ),
        content = content,
    )
}

@Composable
private fun PlanTitleText(type: SubscriptionPlanType) {
    Text(
        text = stringResource(type.toTitle()),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun PlanDescriptionText(type: SubscriptionPlanType) {
    Text(
        text = stringResource(type.toDescription()),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PlanPriceText(
    price: String,
    periodUnit: String,
    priceFontSize: TextUnit,
    priceUnitFontSize: TextUnit,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = priceFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.3).sp,
                ),
            ) {
                append(price)
            }
            withStyle(
                style = SpanStyle(
                    fontSize = priceUnitFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                append(" /$periodUnit")
            }
        },
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun PlanDiscountBadge(
    savePercentage: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(4.dp),
            ).padding(
                horizontal = 8.dp,
                vertical = 3.dp,
            ),
    ) {
        Text(
            text = stringResource(R.string.paywall_save_percent, savePercentage),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp,
        )
    }
}
