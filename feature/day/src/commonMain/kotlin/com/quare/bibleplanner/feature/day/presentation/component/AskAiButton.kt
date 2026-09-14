package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_ask_ai
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AskAiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = dayActionButtonMinHeight),
        shape = RoundedCornerShape(percent = 50),
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        HorizontalSpacer(ButtonDefaults.IconSpacing)
        Text(text = stringResource(Res.string.day_ask_ai))
    }
}
