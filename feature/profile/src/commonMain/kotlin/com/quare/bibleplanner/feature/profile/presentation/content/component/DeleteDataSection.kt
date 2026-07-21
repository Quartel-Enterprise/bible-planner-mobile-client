package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.data_section
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileMenuOptionsFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DeleteDataSection(
    modifier: Modifier = Modifier,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.data_section))
        SectionCard {
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.deleteProgress,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_PROGRESS)) },
                iconColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}
