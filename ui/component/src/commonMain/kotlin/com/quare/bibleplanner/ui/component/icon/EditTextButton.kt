package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.edit
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(stringResource(Res.string.edit))
    }
}
