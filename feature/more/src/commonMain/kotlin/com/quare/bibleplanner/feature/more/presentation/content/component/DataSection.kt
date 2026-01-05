package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.data_section
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DataSection(
    modifier: Modifier = Modifier,
    onEvent: (MoreUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(stringResource(Res.string.data_section))
        SectionCard {
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.deleteProgress,
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.DELETE_PROGRESS)) },
                isDestructive = true,
            )
        }
    }
}
