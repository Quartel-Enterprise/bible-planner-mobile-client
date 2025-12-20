package com.quare.bibleplanner.ui.component.date

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.stringResource

@Composable
fun DateText(
    modifier: Modifier = Modifier,
    model: DatePresentationModel,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        modifier = modifier,
        text = model.toText(),
        style = style,
    )
}

@Composable
private fun DatePresentationModel.toText(): String = "$day ${stringResource(month).take(3)} $year, $hour:$minute"
