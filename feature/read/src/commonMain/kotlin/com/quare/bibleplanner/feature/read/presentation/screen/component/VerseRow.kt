package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import com.quare.bibleplanner.ui.component.highlight.toBackgroundColor
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import com.quare.bibleplanner.ui.theme.font.toFontFamily

private const val LINE_HEIGHT_RATIO = 1.75f
private const val HEADING_FONT_SIZE_RATIO = 0.94f
private const val VERSE_NUMBER_ALPHA = 0.55f
private const val DIMMED_ALPHA = 0.22f
private const val VERSE_NUMBER_FONT_SIZE_RATIO = 0.68f
private const val VERSE_NUMBER_WIDTH_RATIO = 1.2f
private const val SELECTION_UNDERLINE_OFFSET_RATIO = 0.14f
private val verseVerticalPadding = 6.dp
private const val FLASH_MAX_ALPHA = 0.22f
private val flashCornerRadius = 8.dp
private val selectionUnderlineDotWidth = 1.6.dp
private val selectionUnderlineDotGap = 3.dp

@Composable
internal fun VerseRow(
    verse: VerseUiModel,
    settings: ReaderSettingsModel,
    onClick: () -> Unit,
    onNoteIconClick: (VerseNoteMarkUiModel) -> Unit,
    modifier: Modifier = Modifier,
    isDimmed: Boolean = false,
    flashAlpha: (() -> Float)? = null,
) {
    val fontSize = settings.fontSizeSp.sp
    val textStyle = TextStyle(
        fontFamily = settings.font.toFontFamily(),
        fontSize = fontSize,
        lineHeight = fontSize * LINE_HEIGHT_RATIO,
        /*
         * Why: Compose trims leading above the first and below the last line by default, so
         * verses sat closer than their own lines; Trim.None keeps one even rhythm.
         */
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.None,
        ),
        color = MaterialTheme.colorScheme.onSurface,
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .alpha(if (isDimmed) DIMMED_ALPHA else 1f)
            .drawBehind {
                flashAlpha?.let { alpha ->
                    drawRoundRect(
                        color = primaryColor.copy(alpha = alpha() * FLASH_MAX_ALPHA),
                        cornerRadius = CornerRadius(flashCornerRadius.toPx()),
                    )
                }
            },
    ) {
        val noteMark = verse.noteMark?.takeIf { settings.isNoteIconEnabled }
        val isNoteLinkedThroughHeading = noteMark?.position?.isLinkedAbove == true
        verse.heading?.let { heading ->
            /*
             * Why: the heading titles the section, so it stays out of the tap target (it would pick
             * an arbitrary verse) and is selectable so the title can be copied.
             */
            SelectionContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isNoteLinkedThroughHeading) Modifier.drawVerseNoteLink(primaryColor) else Modifier),
            ) {
                Text(
                    modifier = Modifier.padding(
                        top = 18.dp,
                        bottom = 2.dp,
                        end = if (isNoteLinkedThroughHeading) verseNoteIndicatorWidth else 0.dp,
                    ),
                    text = heading,
                    style = textStyle,
                    fontSize = fontSize * HEADING_FONT_SIZE_RATIO,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
        ) {
            VerseLine(
                modifier = Modifier.padding(
                    top = verseVerticalPadding,
                    bottom = verseVerticalPadding,
                    end = if (noteMark == null) 0.dp else verseNoteIndicatorWidth,
                ),
                verse = verse,
                settings = settings,
                textStyle = textStyle,
            )
            noteMark?.let { safeNoteMark ->
                /*
                 * Why: matchParentSize lets the mark span the whole row, padding included, so the
                 * bar of a note over several verses joins the next row without a gap.
                 */
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    VerseNoteIndicator(
                        position = safeNoteMark.position,
                        firstLineHeight = with(LocalDensity.current) { textStyle.lineHeight.toDp() },
                        rowVerticalPadding = verseVerticalPadding,
                        onClick = { onNoteIconClick(safeNoteMark) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VerseLine(
    verse: VerseUiModel,
    settings: ReaderSettingsModel,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val fontSize = textStyle.fontSize
    /*
     * Why: align by baseline, not top: the 1.75 line height puts the first baseline well
     * below the box top, and a fixed offset would drift with text size.
     */
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            modifier = Modifier
                .widthIn(min = (settings.fontSizeSp * VERSE_NUMBER_WIDTH_RATIO).dp)
                .alignByBaseline()
                .alpha(VERSE_NUMBER_ALPHA),
            text = verse.number.toString(),
            fontFamily = displaySerifFontFamily(),
            fontSize = fontSize * VERSE_NUMBER_FONT_SIZE_RATIO,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        Text(
            modifier = Modifier
                .alignByBaseline()
                .drawWithContent {
                    drawContent()
                    if (verse.isSelected) {
                        textLayoutResult?.let { layout ->
                            drawSelectionUnderline(layout, fontSize, textStyle.color)
                        }
                    }
                },
            text = verse.text.withHighlight(verse.highlightColor),
            style = textStyle,
            onTextLayout = { textLayoutResult = it },
        )
    }
}

private fun DrawScope.drawSelectionUnderline(
    layout: TextLayoutResult,
    fontSize: TextUnit,
    color: Color,
) {
    val offsetPx = fontSize.toPx() * SELECTION_UNDERLINE_OFFSET_RATIO
    val dotWidthPx = selectionUnderlineDotWidth.toPx()
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dotWidthPx, selectionUnderlineDotGap.toPx()))
    for (lineIndex in 0 until layout.lineCount) {
        val y = layout.getLineBaseline(lineIndex) + offsetPx
        drawLine(
            color = color,
            start = Offset(x = layout.getLineLeft(lineIndex), y = y),
            end = Offset(x = layout.getLineRight(lineIndex), y = y),
            strokeWidth = dotWidthPx,
            cap = StrokeCap.Round,
            pathEffect = pathEffect,
        )
    }
}

/*
 * Why: a span background hugs the words on every line like a marker, unlike a
 * background on the whole Text.
 */
@Composable
private fun String.withHighlight(highlightColor: HighlightColor?): AnnotatedString = buildAnnotatedString {
    val background = highlightColor?.toBackgroundColor()
    if (background == null) {
        append(this@withHighlight)
    } else {
        withStyle(SpanStyle(background = background)) {
            append(this@withHighlight)
        }
    }
}
