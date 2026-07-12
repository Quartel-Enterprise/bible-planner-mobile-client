package com.quare.bibleplanner.feature.accountdetails.presentation.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_created_at
import bibleplanner.feature.account_details.generated.resources.account_details_last_login
import bibleplanner.feature.account_details.generated.resources.account_details_login_apple
import bibleplanner.feature.account_details.generated.resources.account_details_login_google
import bibleplanner.feature.account_details.generated.resources.account_details_login_method
import bibleplanner.feature.account_details.generated.resources.account_details_login_other
import bibleplanner.feature.account_details.generated.resources.account_details_value_unknown
import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.core.date.toDateRepresentation
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountInfo
import com.quare.bibleplanner.feature.accountdetails.presentation.model.LoginMethod
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.format
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

private const val SHIMMER_ROWS = 3

@Composable
internal fun AccountInfoSection(accountInfo: Loadable<AccountInfo?>) {
    when (accountInfo) {
        Loadable.Loading -> AccountInfoShimmer()
        is Loadable.Loaded -> accountInfo.value?.let { LoadedAccountInfo(it) }
    }
}

@Composable
private fun LoadedAccountInfo(accountInfo: AccountInfo) {
    Column {
        LoginMethodRow(loginMethod = accountInfo.loginMethod)
        InfoRow(
            icon = Icons.Default.Schedule,
            label = stringResource(Res.string.account_details_last_login),
            value = accountInfo.lastSignInAt?.toLastLoginLabel() ?: unknownValue(),
        )
        InfoRow(
            icon = Icons.Default.EventAvailable,
            label = stringResource(Res.string.account_details_created_at),
            value = accountInfo.createdAt?.toCreatedAtLabel() ?: unknownValue(),
        )
    }
}

@Composable
private fun LoginMethodRow(loginMethod: LoginMethod) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalSpacer(14.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.account_details_login_method),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalSpacer(2.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProviderLogo(loginMethod)
                Text(
                    text = loginMethod.label(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ProviderLogo(loginMethod: LoginMethod) {
    when (loginMethod) {
        LoginMethod.GOOGLE -> Image(
            imageVector = googleLogo(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )

        LoginMethod.APPLE -> Icon(
            imageVector = appleLogo(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp),
        )

        LoginMethod.OTHER -> return
    }
    HorizontalSpacer(6.dp)
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalSpacer(14.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalSpacer(2.dp)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AccountInfoShimmer() {
    Column {
        repeat(SHIMMER_ROWS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(modifier = Modifier.size(24.dp))
                HorizontalSpacer(14.dp)
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.35f).height(12.dp))
                    VerticalSpacer(6.dp)
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LoginMethod.label(): String = when (this) {
    LoginMethod.GOOGLE -> stringResource(Res.string.account_details_login_google)
    LoginMethod.APPLE -> stringResource(Res.string.account_details_login_apple)
    LoginMethod.OTHER -> stringResource(Res.string.account_details_login_other)
}

@Composable
private fun unknownValue(): String = stringResource(Res.string.account_details_value_unknown)

@Composable
private fun Instant.toLastLoginLabel(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val dateLabel = dateTime.date.toDateRepresentation().format()
    val time = "${dateTime.hour.padded()}:${dateTime.minute.padded()}"
    return "$dateLabel, $time"
}

@Composable
private fun Instant.toCreatedAtLabel(): String =
    DateRepresentation.Custom(toLocalDateTime(TimeZone.currentSystemDefault()).date).format()

private fun Int.padded(): String = toString().padStart(length = 2, padChar = '0')
