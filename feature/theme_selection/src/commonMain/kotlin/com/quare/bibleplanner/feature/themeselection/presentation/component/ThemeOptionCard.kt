package com.quare.bibleplanner.feature.themeselection.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionModel
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemeOptionCard(
    model: ThemeSelectionModel,
    onClick: (Theme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = model.isActive
    val primary = MaterialTheme.colorScheme.primary
    val borderColor = if (isSelected) primary else Color.Transparent
    val containerColor =
        if (isSelected) primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface

    Card(
        onClick = { onClick(model.preference) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                borderColor
            } else {
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.2f,
                )
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val title = stringResource(model.title)
            Icon(
                imageVector = model.icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
