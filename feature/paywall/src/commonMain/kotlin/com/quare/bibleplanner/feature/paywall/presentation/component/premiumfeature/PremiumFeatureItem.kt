package com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumicon.PremiumCheckIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun PremiumFeatureItem(
    text: String,
    titleColor: Color,
    subtitleColor: Color,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    subtext: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PremiumCheckIcon(modifier = Modifier.size(iconSize))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = titleColor,
            )
            if (subtext != null) {
                VerticalSpacer(2)
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                )
            }
        }
    }
}
