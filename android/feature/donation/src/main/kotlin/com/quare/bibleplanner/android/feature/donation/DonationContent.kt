package com.quare.bibleplanner.android.feature.donation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.icon.SheetIconTile
import com.quare.bibleplanner.feature.donation.presentation.DonationIcons
import com.quare.bibleplanner.feature.donation.presentation.DonationSection
import com.quare.bibleplanner.feature.donation.presentation.DonationType
import com.quare.bibleplanner.feature.donation.presentation.DonationUiEvent
import com.quare.bibleplanner.feature.donation.presentation.DonationUiState
import com.quare.bibleplanner.feature.donation.presentation.address
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private val bitcoinColor = Color(0xFFF7931A)
private val usdtColor = Color(0xFF44A186)
private val pixColor = Color(0xFF32BCAD)
private val gitHubSponsorsColor = Color(0xFFEA4AAA)
private val bodyMaxWidth = 320.dp
private val methodIconTileSize = 40.dp
private val methodIconSize = 24.dp
private const val ICON_TILE_ALPHA = 0.15f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DonationContent(
    uiState: DonationUiState,
    onEvent: (DonationUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    val onDismiss = { sheetCloseGuard.close { onEvent(DonationUiEvent.Dismiss) } }
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
        ) {
            SheetIconTile(imageVector = Icons.Rounded.VolunteerActivism)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.donation_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier.widthIn(max = bodyMaxWidth),
                text = stringResource(R.string.donation_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.sections.forEach { section ->
                    DonationSectionCard(
                        section = section,
                        uiState = uiState,
                        onEvent = onEvent,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.donation_maybe_later))
            }
        }
    }
}

@Composable
private fun DonationSectionCard(
    section: DonationSection,
    uiState: DonationUiState,
    onEvent: (DonationUiEvent) -> Unit,
) {
    when (section) {
        DonationSection.BITCOIN -> ExpandableMethodCard(
            title = stringResource(R.string.donation_bitcoin),
            description = stringResource(R.string.donation_bitcoin_description),
            icon = painterResource(R.drawable.donation_ic_bitcoin),
            iconColor = bitcoinColor,
            isExpanded = uiState.isBitcoinExpanded,
            onClick = { onEvent(DonationUiEvent.ToggleBitcoin) },
        ) {
            CopyAddressItem(
                title = stringResource(R.string.donation_onchain),
                icon = rememberVectorPainter(DonationIcons.OnChain),
                address = DonationType.BTC_ONCHAIN.address,
                type = DonationType.BTC_ONCHAIN,
                copiedType = uiState.copiedType,
                onEvent = onEvent,
            )
            CopyAddressItem(
                title = stringResource(R.string.donation_lightning),
                icon = rememberVectorPainter(DonationIcons.LightningBolt),
                address = DonationType.BTC_LIGHTNING.address,
                type = DonationType.BTC_LIGHTNING,
                copiedType = uiState.copiedType,
                onEvent = onEvent,
            )
        }

        DonationSection.USDT -> ExpandableMethodCard(
            title = stringResource(R.string.donation_usdt),
            description = stringResource(R.string.donation_usdt_description),
            icon = rememberVectorPainter(Icons.Rounded.AttachMoney),
            iconColor = usdtColor,
            isExpanded = uiState.isUsdtExpanded,
            onClick = { onEvent(DonationUiEvent.ToggleUsdt) },
        ) {
            CopyAddressItem(
                title = stringResource(R.string.donation_erc20),
                icon = rememberVectorPainter(DonationIcons.Ethereum),
                address = DonationType.USDT_ERC20.address,
                type = DonationType.USDT_ERC20,
                copiedType = uiState.copiedType,
                onEvent = onEvent,
            )
            CopyAddressItem(
                title = stringResource(R.string.donation_trc20),
                icon = rememberVectorPainter(DonationIcons.Tron),
                address = DonationType.USDT_TRC20.address,
                type = DonationType.USDT_TRC20,
                copiedType = uiState.copiedType,
                onEvent = onEvent,
            )
        }

        DonationSection.PIX -> ExpandableMethodCard(
            title = stringResource(R.string.donation_pix),
            description = stringResource(R.string.donation_pix_description),
            icon = painterResource(R.drawable.donation_ic_pix),
            iconColor = pixColor,
            isExpanded = uiState.isPixExpanded,
            onClick = { onEvent(DonationUiEvent.TogglePix) },
        ) {
            CopyAddressItem(
                title = stringResource(R.string.donation_pix_random_key),
                icon = null,
                address = DonationType.PIX.address,
                type = DonationType.PIX,
                copiedType = uiState.copiedType,
                onEvent = onEvent,
            )
            PixQrCodeItem(onClick = { onEvent(DonationUiEvent.OpenPixQr) })
        }

        DonationSection.GITHUB -> Card(
            onClick = { onEvent(DonationUiEvent.OpenGitHubSponsors) },
            modifier = Modifier.fillMaxWidth(),
            colors = methodCardColors(),
        ) {
            MethodHeaderItem(
                title = stringResource(R.string.donation_gh_sponsors),
                description = stringResource(R.string.donation_gh_sponsors_description),
                icon = rememberVectorPainter(Icons.Rounded.Favorite),
                iconColor = gitHubSponsorsColor,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun ExpandableMethodCard(
    title: String,
    description: String,
    icon: Painter,
    iconColor: Color,
    isExpanded: Boolean,
    onClick: () -> Unit,
    expandedContent: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = methodCardColors(),
    ) {
        MethodHeaderItem(
            title = title,
            description = description,
            icon = icon,
            iconColor = iconColor,
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(rotation),
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                )
            },
        )
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp,
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                expandedContent()
            }
        }
    }
}

@Composable
private fun MethodHeaderItem(
    title: String,
    description: String,
    icon: Painter,
    iconColor: Color,
    trailingIcon: @Composable () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
            )
        },
        supportingContent = { Text(text = description) },
        leadingContent = {
            MethodIconTile(
                icon = icon,
                color = iconColor,
            )
        },
        trailingContent = trailingIcon,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun MethodIconTile(
    icon: Painter,
    color: Color,
) {
    Surface(
        modifier = Modifier.size(methodIconTileSize),
        color = color.copy(alpha = ICON_TILE_ALPHA),
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(methodIconSize),
                painter = icon,
                contentDescription = null,
                tint = color,
            )
        }
    }
}

@Composable
private fun CopyAddressItem(
    title: String,
    icon: Painter?,
    address: String,
    type: DonationType,
    copiedType: DonationType?,
    onEvent: (DonationUiEvent) -> Unit,
) {
    val isCopied = copiedType == type
    ListItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onEvent(DonationUiEvent.Copy(type)) },
        colors = ListItemDefaults.colors(containerColor = copyItemContainerColor(isCopied)),
        headlineContent = { Text(text = title) },
        supportingContent = {
            if (isCopied) {
                Text(
                    text = stringResource(R.string.donation_address_copied),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(text = address)
            }
        },
        leadingContent = icon?.let { painter ->
            {
                Icon(
                    painter = painter,
                    contentDescription = null,
                )
            }
        },
        trailingContent = {
            if (isCopied) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun PixQrCodeItem(onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(text = stringResource(R.string.donation_pix_qr_code)) },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.QrCode,
                contentDescription = null,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun copyItemContainerColor(isCopied: Boolean): Color {
    val color by animateColorAsState(
        if (isCopied) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
    )
    return color
}

@Composable
private fun methodCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
)
