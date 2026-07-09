package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.support_comments_section
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SupportSection(
    modifier: Modifier = Modifier,
    onEvent: (MoreUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.support_comments_section))
        SectionCard {
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.contactSupport,
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.CONTACT_SUPPORT)) },
            )
            HorizontalDivider()
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.rateApp,
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.RATE_APP)) },
                trailingContent = {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                },
            )
        }
    }
}
