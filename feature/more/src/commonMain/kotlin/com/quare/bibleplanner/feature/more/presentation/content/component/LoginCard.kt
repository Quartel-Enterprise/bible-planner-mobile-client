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
import bibleplanner.feature.more.generated.resources.login_card_subtitle
import bibleplanner.feature.more.generated.resources.login_card_title
import coil3.compose.SubcomposeAsyncImage
import com.quare.bibleplanner.feature.more.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginCard(
    accountStatusModel: AccountStatusModel,
    onEvent: (MoreUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .then(
            if (accountStatusModel is AccountStatusModel.LoggedIn) {
                Modifier.clickable { onEvent(MoreUiEvent.OnAccountCardClick) }
            } else {
                Modifier
            },
        )
    ElevatedCard(
        modifier = cardModifier,
        shape = MaterialTheme.shapes.large,
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
                            val photo = accountStatusModel.user.photo
                            if (photo != null) {
                                SubcomposeAsyncImage(
                                    model = photo,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    loading = {
                                        ShimmerBox(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = CircleShape,
                                        )
                                    },
                                )
                            } else {
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
                            AccountStatusModel.Error -> "Error"
                            AccountStatusModel.Loading -> ""
                            is AccountStatusModel.LoggedIn -> accountStatusModel.user.name
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
                            AccountStatusModel.Error -> "There was an error while logging in."
                            AccountStatusModel.Loading -> ""
                            is AccountStatusModel.LoggedIn -> accountStatusModel.user.email
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
