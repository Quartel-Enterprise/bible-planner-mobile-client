package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.change_bible_version
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChangeBibleVersionIconButton(
    modifier: Modifier = Modifier,
    onEvent: (ReadUiEvent) -> Unit,
) {
    CommonIconButton(
        modifier = modifier,
        imageVector = Icons.Default.ChangeCircle,
        onClick = {
            onEvent(ReadUiEvent.ManageBibleVersions)
        },
        contentDescription = stringResource(Res.string.change_bible_version),
    )
}
