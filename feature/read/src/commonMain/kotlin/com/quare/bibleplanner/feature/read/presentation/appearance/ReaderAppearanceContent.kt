package com.quare.bibleplanner.feature.read.presentation.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.font_preview
import bibleplanner.feature.read.generated.resources.reader_focused_verse
import bibleplanner.feature.read.generated.resources.reader_focused_verse_description
import bibleplanner.feature.read.generated.resources.reader_font
import bibleplanner.feature.read.generated.resources.reader_ruler
import bibleplanner.feature.read.generated.resources.reader_ruler_description
import bibleplanner.feature.read.generated.resources.reader_ruler_description_wide
import bibleplanner.feature.read.generated.resources.reader_text_size
import bibleplanner.feature.read.generated.resources.reader_vertical_reading
import bibleplanner.feature.read.generated.resources.reader_vertical_reading_description
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.jetbrains.compose.resources.stringResource

private val smallSampleFontSize = 13.sp
private val largeSampleFontSize = 22.sp
private val fontTileHeight = 62.dp
private const val FONT_TILE_WIDTH_FRACTION = 0.28f

@Composable
internal fun ReaderAppearanceContent(
    uiState: ReaderAppearanceUiState,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWide = LocalIsWideLayout.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextSizeSection(
            fontSizeSp = uiState.settings.fontSizeSp,
            onEvent = onEvent,
        )
        FontSection(
            uiState = uiState,
            onEvent = onEvent,
        )
        SettingSwitchRow(
            title = stringResource(Res.string.reader_ruler),
            description = stringResource(
                if (isWide) Res.string.reader_ruler_description_wide else Res.string.reader_ruler_description,
            ),
            isChecked = uiState.settings.isRulerEnabled,
            onCheckedChange = { isChecked ->
                onEvent(
                    ReaderAppearanceUiEvent.OnFocusAidChange(
                        if (isChecked) ReaderFocusAid.RULER else ReaderFocusAid.NONE,
                    ),
                )
            },
        )
        if (isWide) {
            SettingSwitchRow(
                title = stringResource(Res.string.reader_focused_verse),
                description = stringResource(Res.string.reader_focused_verse_description),
                isChecked = uiState.settings.isFocusedVerseEnabled,
                onCheckedChange = { isChecked ->
                    onEvent(
                        ReaderAppearanceUiEvent.OnFocusAidChange(
                            if (isChecked) ReaderFocusAid.FOCUSED_VERSE else ReaderFocusAid.NONE,
                        ),
                    )
                },
            )
        }
        SettingSwitchRow(
            title = stringResource(Res.string.reader_vertical_reading),
            description = stringResource(Res.string.reader_vertical_reading_description),
            isChecked = uiState.settings.isVerticalReadingEnabled,
            onCheckedChange = { isChecked ->
                onEvent(ReaderAppearanceUiEvent.OnVerticalReadingChange(isChecked))
            },
        )
    }
}

@Composable
private fun TextSizeSection(
    fontSizeSp: Float,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.reader_text_size),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${fontSizeSp}sp",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.font_preview),
                fontSize = smallSampleFontSize,
            )
            Slider(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                value = fontSizeSp,
                onValueChange = { value -> onEvent(ReaderAppearanceUiEvent.OnFontSizeChange(value)) },
                onValueChangeFinished = {
                    onEvent(ReaderAppearanceUiEvent.OnFontSizeChangeFinished(fontSizeSp))
                },
                valueRange = ReaderFontSize.MIN..ReaderFontSize.MAX,
                steps = ((ReaderFontSize.MAX - ReaderFontSize.MIN) / ReaderFontSize.STEP).toInt() - 1,
            )
            Text(
                text = stringResource(Res.string.font_preview),
                fontSize = largeSampleFontSize,
            )
        }
    }
}

@Composable
private fun FontSection(
    uiState: ReaderAppearanceUiState,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onEvent(ReaderAppearanceUiEvent.OnFontMenuToggle(!uiState.isFontMenuExpanded))
                }.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.reader_font),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(uiState.settings.font.labelResource),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = uiState.settings.font.toFontFamily(),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        AnimatedVisibility(visible = uiState.isFontMenuExpanded) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReaderFont.entries.forEach { font ->
                    FontTile(
                        font = font,
                        isSelected = font == uiState.settings.font,
                        onClick = { onEvent(ReaderAppearanceUiEvent.OnFontClick(font)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FontTile(
    font: ReaderFont,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(FONT_TILE_WIDTH_FRACTION).height(fontTileHeight),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Column(
            modifier = Modifier.clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.font_preview),
                fontFamily = font.toFontFamily(),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(font.labelResource),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
    }
}
