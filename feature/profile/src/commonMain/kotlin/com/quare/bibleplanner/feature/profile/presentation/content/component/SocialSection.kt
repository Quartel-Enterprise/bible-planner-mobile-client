package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.social_section
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileMenuOptionsFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SocialSection(
    modifier: Modifier = Modifier,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.social_section))
        SectionCard {
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.instagram,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.INSTAGRAM)) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
