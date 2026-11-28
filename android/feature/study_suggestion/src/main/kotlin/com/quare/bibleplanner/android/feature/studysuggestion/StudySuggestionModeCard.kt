package com.quare.bibleplanner.android.feature.studysuggestion

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode

private val cardShape = RoundedCornerShape(14.dp)
private const val SELECTED_CONTAINER_ALPHA = 0.12f

@Composable
internal fun StudySuggestionModeCard(
    mode: StudySuggestionMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(if (isSelected) primary.copy(alpha = SELECTED_CONTAINER_ALPHA) else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isSelected) primary else MaterialTheme.colorScheme.outlineVariant,
                shape = cardShape,
            ).selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            ).padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StudySuggestionModePreviewBox(mode = mode)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = stringResource(mode.toTitleRes()),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(mode.toDescriptionRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = null,
        )
    }
}

@StringRes
private fun StudySuggestionMode.toTitleRes(): Int = when (this) {
    StudySuggestionMode.DIALOG -> R.string.study_suggestion_mode_dialog_title
    StudySuggestionMode.BANNER -> R.string.study_suggestion_mode_banner_title
}

@StringRes
private fun StudySuggestionMode.toDescriptionRes(): Int = when (this) {
    StudySuggestionMode.DIALOG -> R.string.study_suggestion_mode_dialog_description
    StudySuggestionMode.BANNER -> R.string.study_suggestion_mode_banner_description
}
