package com.quare.bibleplanner.feature.day.presentation.component.completeddate

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.quare.bibleplanner.feature.day.presentation.model.DatePresentationModel
import com.quare.bibleplanner.ui.utils.toStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DateText(
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
private fun DatePresentationModel.toText(): String =
    "$day ${stringResource(month.toStringResource()).take(3)} $year, $hour:$minute"
