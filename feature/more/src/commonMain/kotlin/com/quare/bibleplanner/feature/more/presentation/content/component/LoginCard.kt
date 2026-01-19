package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.login_card_button
import bibleplanner.feature.more.generated.resources.login_card_subtitle
import bibleplanner.feature.more.generated.resources.login_card_title
import com.quare.bibleplanner.feature.more.domain.model.AccountStatusModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginCard(
    accountStatusModel: AccountStatusModel,
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    when (accountStatusModel) {
                        AccountStatusModel.Error -> Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            tint = MaterialTheme.colorScheme.error,
                        )

                        AccountStatusModel.Loading -> CircularProgressIndicator(modifier = Modifier.size(48.dp))

                        is AccountStatusModel.LoggedIn -> Unit

                        AccountStatusModel.LoggedOut -> Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                headlineContent = {
                    val text = when (accountStatusModel) {
                        AccountStatusModel.Error -> "Error"
                        AccountStatusModel.Loading -> "Loading..."
                        is AccountStatusModel.LoggedIn -> accountStatusModel.user.name
                        AccountStatusModel.LoggedOut -> stringResource(Res.string.login_card_title)
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                supportingContent = {
                    val supportingText = when (accountStatusModel) {
                        AccountStatusModel.Error -> "There was an error while logging in."
                        AccountStatusModel.Loading -> "Please wait while we log you in."
                        is AccountStatusModel.LoggedIn -> accountStatusModel.user.email
                        AccountStatusModel.LoggedOut -> stringResource(Res.string.login_card_subtitle)
                    }
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            if (accountStatusModel is AccountStatusModel.LoggedOut) {
                Button(
                    onClick = onLoginClick,
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
