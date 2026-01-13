package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.privacy_policy
import bibleplanner.feature.more.generated.resources.terms_of_service
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LegalSection(
    modifier: Modifier = Modifier,
    onEvent: (MoreUiEvent) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.PRIVACY_POLICY)) }) {
            Text(stringResource(Res.string.privacy_policy))
        }
        Text("•")
        TextButton(onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.TERMS)) }) {
            Text(stringResource(Res.string.terms_of_service))
        }
    }
}
