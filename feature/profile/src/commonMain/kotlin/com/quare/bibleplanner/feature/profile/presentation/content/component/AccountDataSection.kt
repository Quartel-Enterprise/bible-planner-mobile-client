package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.account_data_section
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileMenuOptionsFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun AccountDataSection(
    isLoggedIn: Boolean,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.account_data_section))
        SectionCard {
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.deleteProgress,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_PROGRESS)) },
            )
            if (isLoggedIn) {
                HorizontalDivider()
                ProfileMenuItem(
                    itemModel = ProfileMenuOptionsFactory.deleteAccount,
                    onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_ACCOUNT)) },
                    iconColor = MaterialTheme.colorScheme.error,
                    headlineColor = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
