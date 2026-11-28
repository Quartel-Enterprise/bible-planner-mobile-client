package com.quare.bibleplanner.android.feature.accountdetails

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountInfo
import com.quare.bibleplanner.feature.accountdetails.presentation.model.LoginMethod
import software.quare.loadable.Loadable

private const val SKELETON_ROWS = 3
private val providerLogoSize = 18.dp

@Composable
internal fun AccountInfoSection(accountInfo: Loadable<AccountInfo?>) {
    when (accountInfo) {
        Loadable.Loading -> AccountInfoSkeleton()
        is Loadable.Loaded -> accountInfo.value?.let { info -> LoadedAccountInfoColumn(info) }
    }
}

@Composable
private fun LoadedAccountInfoColumn(accountInfo: AccountInfo) {
    val unknownValue = stringResource(R.string.account_details_value_unknown)
    Column {
        InfoRow(
            icon = Icons.Outlined.VerifiedUser,
            label = stringResource(R.string.account_details_login_method),
        ) {
            LoginMethodLabel(loginMethod = accountInfo.loginMethod)
        }
        InfoRow(
            icon = Icons.Outlined.Schedule,
            label = stringResource(R.string.account_details_last_login),
        ) {
            Text(text = accountInfo.lastSignInAt?.let { instant -> rememberLastLoginLabel(instant) } ?: unknownValue)
        }
        InfoRow(
            icon = Icons.Outlined.EventAvailable,
            label = stringResource(R.string.account_details_created_at),
        ) {
            Text(text = accountInfo.createdAt?.let { instant -> rememberCreatedAtLabel(instant) } ?: unknownValue)
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: @Composable () -> Unit,
) {
    ListItem(
        overlineContent = { Text(text = label) },
        headlineContent = value,
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun LoginMethodLabel(loginMethod: LoginMethod) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ProviderLogoIcon(loginMethod = loginMethod)
        Text(text = stringResource(loginMethod.toLabel()))
    }
}

@Composable
private fun ProviderLogoIcon(loginMethod: LoginMethod) {
    when (loginMethod) {
        LoginMethod.GOOGLE -> Image(
            painter = painterResource(R.drawable.account_details_google_logo),
            contentDescription = null,
            modifier = Modifier.size(providerLogoSize),
        )

        LoginMethod.APPLE -> Icon(
            painter = painterResource(R.drawable.account_details_apple_logo),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(providerLogoSize),
        )

        LoginMethod.OTHER -> return
    }
    Spacer(modifier = Modifier.width(6.dp))
}

@Composable
private fun AccountInfoSkeleton() {
    Column {
        repeat(SKELETON_ROWS) {
            ListItem(
                overlineContent = {
                    PlaceholderBox(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(12.dp),
                    )
                },
                headlineContent = {
                    PlaceholderBox(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth(0.6f)
                            .height(16.dp),
                    )
                },
                leadingContent = { PlaceholderBox(modifier = Modifier.size(24.dp)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@StringRes
private fun LoginMethod.toLabel(): Int = when (this) {
    LoginMethod.GOOGLE -> R.string.account_details_login_google
    LoginMethod.APPLE -> R.string.account_details_login_apple
    LoginMethod.OTHER -> R.string.account_details_login_other
}
