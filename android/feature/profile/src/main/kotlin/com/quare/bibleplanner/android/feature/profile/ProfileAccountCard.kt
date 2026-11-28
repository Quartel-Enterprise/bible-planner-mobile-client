package com.quare.bibleplanner.android.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.avatar.ProfileAvatarImage
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent

private val avatarSize = 48.dp

@Composable
internal fun ProfileAccountCard(
    accountStatusModel: AccountStatusModel,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content: @Composable () -> Unit = {
        Column {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    AccountLeadingIcon(
                        accountStatusModel = accountStatusModel,
                        onAvatarClick = { onEvent(ProfileUiEvent.OnAvatarClick) },
                    )
                },
                headlineContent = { AccountHeadlineText(accountStatusModel = accountStatusModel) },
                supportingContent = { AccountSupportingText(accountStatusModel = accountStatusModel) },
                trailingContent = if (accountStatusModel is AccountStatusModel.LoggedIn) {
                    { ProfileEditButton(onClick = { onEvent(ProfileUiEvent.OnEditProfileClick) }) }
                } else {
                    null
                },
            )
            if (accountStatusModel == AccountStatusModel.LoggedOut) {
                Button(
                    onClick = { onEvent(ProfileUiEvent.OnLoginClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                ) {
                    Text(text = stringResource(R.string.profile_login_card_button))
                }
            }
        }
    }
    if (accountStatusModel is AccountStatusModel.LoggedIn) {
        ElevatedCard(
            onClick = { onEvent(ProfileUiEvent.OnAccountCardClick) },
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
        ) { content() }
    } else {
        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
        ) { content() }
    }
}

@Composable
private fun AccountLeadingIcon(
    accountStatusModel: AccountStatusModel,
    onAvatarClick: () -> Unit,
) {
    when (accountStatusModel) {
        AccountStatusModel.Error -> Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(avatarSize),
            tint = MaterialTheme.colorScheme.error,
        )

        AccountStatusModel.Loading -> ProfileSkeletonBox(
            modifier = Modifier.size(avatarSize),
            shape = CircleShape,
        )

        is AccountStatusModel.LoggedIn -> {
            val profile = accountStatusModel.profile
            ProfileAvatarImage(
                photoUrl = profile.avatar.photoUrl,
                photoBytes = profile.avatar.photoBytes,
                displayName = profile.displayName,
                size = avatarSize,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick),
            )
        }

        AccountStatusModel.LoggedOut -> Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(avatarSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AccountHeadlineText(accountStatusModel: AccountStatusModel) {
    val text = when (accountStatusModel) {
        AccountStatusModel.Error -> stringResource(R.string.profile_login_card_error_title)

        AccountStatusModel.Loading -> {
            ProfileSkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(18.dp))
            return
        }

        is AccountStatusModel.LoggedIn ->
            accountStatusModel.profile.displayName ?: stringResource(R.string.profile_login_card_no_name)

        AccountStatusModel.LoggedOut -> stringResource(R.string.profile_login_card_title)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun AccountSupportingText(accountStatusModel: AccountStatusModel) {
    val text = when (accountStatusModel) {
        AccountStatusModel.Error -> stringResource(R.string.profile_login_card_error_subtitle)

        AccountStatusModel.Loading -> {
            ProfileSkeletonBox(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(0.7f)
                    .height(14.dp),
            )
            return
        }

        is AccountStatusModel.LoggedIn -> accountStatusModel.profile.email

        AccountStatusModel.LoggedOut -> stringResource(R.string.profile_login_card_subtitle)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
