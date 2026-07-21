package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.support_comments_section
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileMenuOptionsFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SupportSection(
    modifier: Modifier = Modifier,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.support_comments_section))
        SectionCard {
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.contactSupport,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CONTACT_SUPPORT)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.rateApp,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.RATE_APP)) },
                trailingContent = {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                },
            )
        }
    }
}
