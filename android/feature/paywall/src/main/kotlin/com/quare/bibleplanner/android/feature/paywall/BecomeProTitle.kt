package com.quare.bibleplanner.android.feature.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
internal fun BecomeProTitle(
    fontSize: TextUnit,
    titleColor: Color,
    proColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.paywall_title_part_1),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
        Text(
            text = stringResource(R.string.paywall_title_part_2),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = proColor,
        )
    }
}
