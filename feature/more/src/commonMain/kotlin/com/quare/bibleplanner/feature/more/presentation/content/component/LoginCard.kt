package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.login_card_button
import bibleplanner.feature.more.generated.resources.login_card_error_subtitle
import bibleplanner.feature.more.generated.resources.login_card_error_title
import bibleplanner.feature.more.generated.resources.login_card_no_name
import bibleplanner.feature.more.generated.resources.login_card_subtitle
import bibleplanner.feature.more.generated.resources.login_card_title
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.feature.more.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.ui.component.ProfileAvatar
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import org.jetbrains.compose.resources.stringResource

private val avatarSize = 48.dp

@Composable
internal fun LoginCard(
    accountStatusModel: AccountStatusModel,
    onEvent: (MoreUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCardContainer(
        isClickable = accountStatusModel is AccountStatusModel.LoggedIn,
        onClick = { onEvent(MoreUiEvent.OnAccountCardClick) },
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    when (accountStatusModel) {
                        AccountStatusModel.Error -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }

                        AccountStatusModel.Loading -> {
                            ShimmerBox(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                            )
                        }

                        is AccountStatusModel.LoggedIn -> {
                            val profile = accountStatusModel.profile
                            ProfileAvatar(
                                photoUrl = (profile.avatar as? AvatarSource.Remote)?.url,
                                photoBytes = (profile.avatar as? AvatarSource.Pending)?.bytes,
                                displayName = profile.displayName,
                                size = avatarSize,
                                onClick = { onEvent(MoreUiEvent.OnAvatarClick) },
                            )
                        }

                        AccountStatusModel.LoggedOut -> {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                headlineContent = {
                    if (accountStatusModel == AccountStatusModel.Loading) {
                        ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(18.dp))
                    } else {
                        val text = when (accountStatusModel) {
                            AccountStatusModel.Error -> stringResource(Res.string.login_card_error_title)

                            AccountStatusModel.Loading -> ""

                            is AccountStatusModel.LoggedIn ->
                                accountStatusModel.profile.displayName
                                    ?: stringResource(Res.string.login_card_no_name)

                            AccountStatusModel.LoggedOut -> stringResource(Res.string.login_card_title)
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
                },
                supportingContent = {
                    if (accountStatusModel == AccountStatusModel.Loading) {
                        ShimmerBox(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth(0.7f)
                                .height(14.dp),
                        )
                    } else {
                        val supportingText = when (accountStatusModel) {
                            AccountStatusModel.Error -> stringResource(Res.string.login_card_error_subtitle)
                            AccountStatusModel.Loading -> ""
                            is AccountStatusModel.LoggedIn -> accountStatusModel.profile.email
                            AccountStatusModel.LoggedOut -> stringResource(Res.string.login_card_subtitle)
                        }
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                trailingContent = {
                    if (accountStatusModel is AccountStatusModel.LoggedIn) {
                        EditProfileButton(onClick = { onEvent(MoreUiEvent.OnEditProfileClick) })
                    }
                },
            )
            if (accountStatusModel == AccountStatusModel.LoggedOut) {
                Button(
                    onClick = { onEvent(MoreUiEvent.OnLoginClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(text = stringResource(Res.string.login_card_button))
                }
            }
        }
    }
}
