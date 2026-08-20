package com.quare.bibleplanner.feature.read.presentation.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
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
import bibleplanner.feature.read.generated.resources.text_size_sample
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.ui.component.icon.ArrowRotationIcon
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.jetbrains.compose.resources.stringResource

private const val TRACK_ALPHA = 0.16f
private const val FONT_TILE_WIDTH_FRACTION = 0.28f
private val smallSampleFontSize = 13.sp
private val largeSampleFontSize = 22.sp
private val fontTileHeight = 62.dp
private val cardShape = RoundedCornerShape(16.dp)
private val rowIconSize = 20.dp
private val trackHeight = 4.dp
private val thumbSize = 20.dp

/**
 * Every setting sits on its own card, the way the design groups them, so the sheet reads as a short
 * list of things to change rather than as a form.
 */
@Composable
internal fun ReaderAppearanceContent(
    uiState: ReaderAppearanceUiState,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWide = LocalIsWideLayout.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextSizeCard(
            fontSizeSp = uiState.settings.fontSizeSp,
            onEvent = onEvent,
        )
        FontCard(
            uiState = uiState,
            onEvent = onEvent,
        )
        SettingSwitchCard(
            icon = Icons.Default.Straighten,
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
            SettingSwitchCard(
                icon = Icons.Default.CenterFocusStrong,
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
        SettingSwitchCard(
            icon = Icons.Default.SwipeVertical,
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
private fun TextSizeCard(
    fontSizeSp: Float,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    SettingCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SampleLetter(fontSize = smallSampleFontSize)
                TextSizeSlider(
                    modifier = Modifier.weight(1f),
                    fontSizeSp = fontSizeSp,
                    onEvent = onEvent,
                )
                SampleLetter(fontSize = largeSampleFontSize)
            }
        }
    }
}

@Composable
private fun SampleLetter(fontSize: TextUnit) {
    Text(
        text = stringResource(Res.string.text_size_sample),
        fontFamily = displaySerifFontFamily(),
        fontSize = fontSize,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * A plain track and a round knob: the stock slider marks every step it can stop at, which turns a
 * twenty-position range into a row of dots. The steps still quantise the value, they are just not
 * drawn.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextSizeSlider(
    fontSizeSp: Float,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Slider(
        modifier = modifier,
        value = fontSizeSp,
        onValueChange = { value -> onEvent(ReaderAppearanceUiEvent.OnFontSizeChange(value)) },
        onValueChangeFinished = {
            onEvent(ReaderAppearanceUiEvent.OnFontSizeChangeFinished(fontSizeSp))
        },
        valueRange = ReaderFontSize.MIN..ReaderFontSize.MAX,
        steps = ((ReaderFontSize.MAX - ReaderFontSize.MIN) / ReaderFontSize.STEP).toInt() - 1,
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = TRACK_ALPHA)),
            )
        },
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
            )
        },
    )
}

@Composable
private fun FontCard(
    uiState: ReaderAppearanceUiState,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    SettingCard {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onEvent(ReaderAppearanceUiEvent.OnFontMenuToggle(!uiState.isFontMenuExpanded))
                    }.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RowIcon(Icons.Default.TextFields)
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.reader_font),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(uiState.settings.font.labelResource),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = uiState.settings.font.toFontFamily(),
                    color = MaterialTheme.colorScheme.primary,
                )
                ArrowRotationIcon(isUp = uiState.isFontMenuExpanded)
            }
            AnimatedVisibility(visible = uiState.isFontMenuExpanded) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp,
                    ),
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
            MaterialTheme.colorScheme.surfaceContainerHighest
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
private fun SettingSwitchCard(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RowIcon(icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
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
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Icon(
        modifier = Modifier.size(rowIconSize),
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        content()
    }
}
