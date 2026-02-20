package com.quare.bibleplanner.feature.donation.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.donation.generated.resources.Res
import bibleplanner.feature.donation.generated.resources.donation_bitcoin
import bibleplanner.feature.donation.generated.resources.donation_bitcoin_description
import bibleplanner.feature.donation.generated.resources.donation_description
import bibleplanner.feature.donation.generated.resources.donation_erc20
import bibleplanner.feature.donation.generated.resources.donation_gh_sponsors
import bibleplanner.feature.donation.generated.resources.donation_gh_sponsors_description
import bibleplanner.feature.donation.generated.resources.donation_lightning
import bibleplanner.feature.donation.generated.resources.donation_maybe_later
import bibleplanner.feature.donation.generated.resources.donation_onchain
import bibleplanner.feature.donation.generated.resources.donation_pix
import bibleplanner.feature.donation.generated.resources.donation_pix_description
import bibleplanner.feature.donation.generated.resources.donation_pix_qr_code
import bibleplanner.feature.donation.generated.resources.donation_pix_random_key
import bibleplanner.feature.donation.generated.resources.donation_title
import bibleplanner.feature.donation.generated.resources.donation_trc20
import bibleplanner.feature.donation.generated.resources.donation_usdt
import bibleplanner.feature.donation.generated.resources.donation_usdt_description
import bibleplanner.feature.donation.generated.resources.ic_bitcoin
import bibleplanner.feature.donation.generated.resources.ic_pix
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage
import com.quare.bibleplanner.feature.donation.generated.DonationBuildKonfig
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DonationBottomSheetContent(
    state: DonationUiState,
    onEvent: (DonationUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.donation_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        VerticalSpacer(8.dp)

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.donation_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        VerticalSpacer(24.dp)

        val bitcoinSection = @Composable {
            DonationItem(
                title = stringResource(Res.string.donation_bitcoin),
                description = stringResource(Res.string.donation_bitcoin_description),
                icon = painterResource(Res.drawable.ic_bitcoin),
                iconColor = Color(0xFFF7931A),
                isExpanded = state.isBitcoinExpanded,
                onClick = { onEvent(DonationUiEvent.ToggleBitcoin) },
                expandedContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SubDonationItem(
                            title = stringResource(Res.string.donation_onchain),
                            icon = rememberVectorPainter(DonationIcons.OnChain),
                            description = DonationBuildKonfig.BTC_ONCHAIN,
                            isCopied = state.copiedType == DonationType.BTC_ONCHAIN,
                            onCopy = { onEvent(DonationUiEvent.Copy(DonationType.BTC_ONCHAIN)) },
                        )
                        SubDonationItem(
                            title = stringResource(Res.string.donation_lightning),
                            icon = rememberVectorPainter(DonationIcons.LightningBolt),
                            description = DonationBuildKonfig.BTC_LIGHTNING,
                            isCopied = state.copiedType == DonationType.BTC_LIGHTNING,
                            onCopy = { onEvent(DonationUiEvent.Copy(DonationType.BTC_LIGHTNING)) },
                        )
                    }
                },
            )
        }

        val usdtSection = @Composable {
            DonationItem(
                title = stringResource(Res.string.donation_usdt),
                description = stringResource(Res.string.donation_usdt_description),
                icon = rememberVectorPainter(Icons.Default.AttachMoney),
                iconColor = Color(0xFF44A186),
                isExpanded = state.isUsdtExpanded,
                onClick = { onEvent(DonationUiEvent.ToggleUsdt) },
                expandedContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SubDonationItem(
                            title = stringResource(Res.string.donation_erc20),
                            icon = rememberVectorPainter(DonationIcons.Ethereum),
                            description = DonationBuildKonfig.USDT_ERC20,
                            isCopied = state.copiedType == DonationType.USDT_ERC20,
                            onCopy = { onEvent(DonationUiEvent.Copy(DonationType.USDT_ERC20)) },
                        )
                        SubDonationItem(
                            title = stringResource(Res.string.donation_trc20),
                            icon = rememberVectorPainter(DonationIcons.Tron),
                            description = DonationBuildKonfig.USDT_TRC20,
                            isCopied = state.copiedType == DonationType.USDT_TRC20,
                            onCopy = { onEvent(DonationUiEvent.Copy(DonationType.USDT_TRC20)) },
                        )
                    }
                },
            )
        }

        val pixSection = @Composable {
            DonationItem(
                title = stringResource(Res.string.donation_pix),
                description = stringResource(Res.string.donation_pix_description),
                icon = painterResource(Res.drawable.ic_pix),
                iconColor = Color(0xFF32BCAD),
                isExpanded = state.isPixExpanded,
                onClick = { onEvent(DonationUiEvent.TogglePix) },
                expandedContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SubDonationItem(
                            title = stringResource(Res.string.donation_pix_random_key),
                            icon = null,
                            description = DonationBuildKonfig.PIX_KEY,
                            isCopied = state.copiedType == DonationType.PIX,
                            onCopy = { onEvent(DonationUiEvent.Copy(DonationType.PIX)) },
                        )
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(Res.string.donation_pix_qr_code),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onEvent(DonationUiEvent.OpenPixQr) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                },
            )
        }

        val githubSection = @Composable {
            DonationItem(
                title = stringResource(Res.string.donation_gh_sponsors),
                description = stringResource(Res.string.donation_gh_sponsors_description),
                icon = rememberVectorPainter(Icons.Default.Favorite),
                iconColor = Color(0xFFEA4AAA),
                trailingIcon = rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew),
                onClick = { onEvent(DonationUiEvent.OpenGitHubSponsors) },
            )
        }

        if (getCurrentLanguage() == Language.PORTUGUESE_BRAZIL) {
            pixSection()
            VerticalSpacer(12.dp)
            bitcoinSection()
            VerticalSpacer(12.dp)
            usdtSection()
        } else {
            bitcoinSection()
            VerticalSpacer(12.dp)
            usdtSection()
            VerticalSpacer(12.dp)
            pixSection()
        }

        VerticalSpacer(12.dp)
        githubSection()

        VerticalSpacer(32.dp)

        TextButton(
            onClick = { onEvent(DonationUiEvent.Dismiss) },
        ) {
            Text(
                text = stringResource(Res.string.donation_maybe_later),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
