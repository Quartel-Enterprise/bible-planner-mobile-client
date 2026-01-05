package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.legal_section
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LegalSection(
    modifier: Modifier = Modifier,
    onEvent: (MoreUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(stringResource(Res.string.legal_section))
        SectionCard {
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.privacyPolicy,
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.PRIVACY_POLICY)) },
            )
            HorizontalDivider()
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.termsOfService,
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.TERMS)) },
            )
        }
    }
}
