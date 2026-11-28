package com.quare.bibleplanner.android.feature.readerappearance

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceUiEvent
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceUiState
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private const val FONT_TILE_WIDTH_FRACTION = 0.3f
private const val EXPANDED_ARROW_DEGREES = 180f
private val smallSampleFontSize = 13.sp
private val largeSampleFontSize = 22.sp
private val fontTileHeight = 64.dp
private val fontTileShape = RoundedCornerShape(12.dp)
private val cardShape = RoundedCornerShape(16.dp)
private val horizontalPadding = 16.dp
private val cardSpacing = 8.dp
private val bottomPadding = 24.dp
private val rulerLineOptions = (ReaderRulerLines.MIN..ReaderRulerLines.MAX).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderAppearanceContent(
    uiState: ReaderAppearanceUiState,
    isWideLayout: Boolean,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = { sheetCloseGuard.close { onEvent(ReaderAppearanceUiEvent.OnDismiss) } },
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(cardSpacing),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = cardSpacing),
                text = stringResource(R.string.reader_appearance_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            ReaderAppearanceSettingsSection(
                uiState = uiState,
                isWideLayout = isWideLayout,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ReaderAppearanceSettingsSection(
    uiState: ReaderAppearanceUiState,
    isWideLayout: Boolean,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    val settings = uiState.settings
    TextSizeCard(
        fontSizeSp = settings.fontSizeSp,
        onEvent = onEvent,
    )
    FontCard(
        selectedFont = settings.font,
        isExpanded = uiState.isFontMenuExpanded,
        onEvent = onEvent,
    )
    SettingSwitchCard(
        icon = Icons.AutoMirrored.Outlined.StickyNote2,
        title = stringResource(R.string.reader_appearance_note_icon),
        description = stringResource(R.string.reader_appearance_note_icon_description),
        isChecked = settings.isNoteIconEnabled,
        onCheckedChange = { isChecked -> onEvent(ReaderAppearanceUiEvent.OnNoteIconChange(isChecked)) },
    )
    SettingSwitchCard(
        icon = Icons.Default.Straighten,
        title = stringResource(R.string.reader_appearance_ruler),
        description = stringResource(
            if (isWideLayout) {
                R.string.reader_appearance_ruler_description_wide
            } else {
                R.string.reader_appearance_ruler_description
            },
        ),
        isChecked = settings.isRulerEnabled,
        onCheckedChange = { isChecked ->
            onEvent(
                ReaderAppearanceUiEvent.OnFocusAidChange(
                    if (isChecked) ReaderFocusAid.RULER else ReaderFocusAid.NONE,
                ),
            )
        },
    ) {
        AnimatedVisibility(visible = settings.isRulerEnabled) {
            RulerHeightRow(
                lines = settings.rulerLines,
                onEvent = onEvent,
            )
        }
    }
    if (isWideLayout) {
        SettingSwitchCard(
            icon = Icons.Default.CenterFocusStrong,
            title = stringResource(R.string.reader_appearance_focused_verse),
            description = stringResource(R.string.reader_appearance_focused_verse_description),
            isChecked = settings.isFocusedVerseEnabled,
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
        title = stringResource(R.string.reader_appearance_vertical_reading),
        description = stringResource(R.string.reader_appearance_vertical_reading_description),
        isChecked = settings.isVerticalReadingEnabled,
        onCheckedChange = { isChecked -> onEvent(ReaderAppearanceUiEvent.OnVerticalReadingChange(isChecked)) },
    )
}

@Composable
private fun TextSizeCard(
    fontSizeSp: Float,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    SettingCard {
        Column(
            modifier = Modifier.padding(
                horizontal = horizontalPadding,
                vertical = 14.dp,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.reader_appearance_text_size),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${fontSizeSp}sp",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SampleLetterText(fontSize = smallSampleFontSize)
                TextSizeSlider(
                    modifier = Modifier.weight(1f),
                    fontSizeSp = fontSizeSp,
                    onEvent = onEvent,
                )
                SampleLetterText(fontSize = largeSampleFontSize)
            }
        }
    }
}

@Composable
private fun SampleLetterText(fontSize: TextUnit) {
    Text(
        text = stringResource(R.string.reader_appearance_text_size_sample),
        fontFamily = displaySerifFontFamily(),
        fontSize = fontSize,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/*
 * Why: the stock slider draws a tick for every step, turning a twenty-position range into a row
 * of beads; steps still quantise the value, they are just not drawn.
 */
@Composable
private fun TextSizeSlider(
    fontSizeSp: Float,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Why: fontSizeSp only updates after the async write, so on release it can still hold an older drag value.
    var lastDraggedValue by remember { mutableFloatStateOf(fontSizeSp) }
    Slider(
        modifier = modifier,
        value = fontSizeSp,
        onValueChange = { value ->
            lastDraggedValue = value
            onEvent(ReaderAppearanceUiEvent.OnFontSizeChange(value))
        },
        onValueChangeFinished = { onEvent(ReaderAppearanceUiEvent.OnFontSizeChangeFinished(lastDraggedValue)) },
        valueRange = ReaderFontSize.MIN..ReaderFontSize.MAX,
        steps = ((ReaderFontSize.MAX - ReaderFontSize.MIN) / ReaderFontSize.STEP).toInt() - 1,
        colors = SliderDefaults.colors(
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        ),
    )
}

@Composable
private fun FontCard(
    selectedFont: ReaderFont,
    isExpanded: Boolean,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) EXPANDED_ARROW_DEGREES else 0f,
        label = "fontArrowRotation",
    )
    SettingCard {
        Column {
            ListItem(
                modifier = Modifier.toggleable(
                    value = isExpanded,
                    role = Role.DropdownList,
                    onValueChange = { isChecked -> onEvent(ReaderAppearanceUiEvent.OnFontMenuToggle(isChecked)) },
                ),
                colors = transparentListItemColors(),
                leadingContent = { RowIcon(Icons.Default.TextFields) },
                headlineContent = {
                    Text(
                        text = stringResource(R.string.reader_appearance_font),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(selectedFont.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = selectedFont.toFontFamily(),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Icon(
                            modifier = Modifier.rotate(arrowRotation),
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                        )
                    }
                },
            )
            AnimatedVisibility(visible = isExpanded) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            bottom = 12.dp,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReaderFont.entries.forEach { font ->
                        FontTile(
                            font = font,
                            isSelected = font == selectedFont,
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
        modifier = Modifier
            .fillMaxWidth(FONT_TILE_WIDTH_FRACTION)
            .height(fontTileHeight),
        shape = fontTileShape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    ) {
        Column(
            modifier = Modifier.selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.reader_appearance_font_preview),
                fontFamily = font.toFontFamily(),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(font.labelRes),
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
    extraContent: @Composable () -> Unit = {},
) {
    SettingCard {
        Column {
            ListItem(
                modifier = Modifier.toggleable(
                    value = isChecked,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ),
                colors = transparentListItemColors(),
                leadingContent = { RowIcon(icon) },
                headlineContent = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                supportingContent = { Text(text = description) },
                trailingContent = {
                    Switch(
                        checked = isChecked,
                        onCheckedChange = null,
                    )
                },
            )
            extraContent()
        }
    }
}

@Composable
private fun RulerHeightRow(
    lines: Int,
    onEvent: (ReaderAppearanceUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.padding(
            start = horizontalPadding,
            end = horizontalPadding,
            bottom = 14.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.reader_appearance_ruler_height),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.reader_appearance_ruler_lines,
                    lines,
                    lines,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            rulerLineOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == lines,
                    onClick = { onEvent(ReaderAppearanceUiEvent.OnRulerLinesChange(option)) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = rulerLineOptions.size,
                    ),
                ) {
                    Text(text = option.toString())
                }
            }
        }
    }
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Icon(
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

@Composable
private fun transparentListItemColors(): ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

@get:StringRes
private val ReaderFont.labelRes: Int
    get() = when (this) {
        ReaderFont.LORA -> R.string.reader_appearance_font_lora
        ReaderFont.LITERATA -> R.string.reader_appearance_font_literata
        ReaderFont.GARAMOND -> R.string.reader_appearance_font_garamond
        ReaderFont.CRIMSON -> R.string.reader_appearance_font_crimson
        ReaderFont.BITTER -> R.string.reader_appearance_font_bitter
        ReaderFont.SYSTEM -> R.string.reader_appearance_font_system
        ReaderFont.NUNITO -> R.string.reader_appearance_font_nunito
        ReaderFont.ATKINSON -> R.string.reader_appearance_font_atkinson
        ReaderFont.DYSLEXIC -> R.string.reader_appearance_font_dyslexic
    }
