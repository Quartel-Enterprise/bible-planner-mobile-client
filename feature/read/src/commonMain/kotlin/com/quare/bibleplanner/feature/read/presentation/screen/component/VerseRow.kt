package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
private val selectionUnderlineDotWidth = 1.6.dp
private val selectionUnderlineDotGap = 3.dp

/**
 * A verse and, when the section starts here, its pericope heading. Tapping anywhere on the row
 * selects the verse — the whole row is the target, not just the words.
 *
 * The verse number's size and gutter are fractions of the reader's text size, so they grow with it
 * instead of shrinking into the margin.
 */
@Composable
internal fun VerseRow(
    verse: VerseUiModel,
    settings: ReaderSettingsModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDimmed: Boolean = false,
) {
    val fontSize = settings.fontSizeSp.sp
    val textStyle = TextStyle(
        fontFamily = settings.font.toFontFamily(),
        fontSize = fontSize,
        lineHeight = fontSize * LINE_HEIGHT_RATIO,
        /*
         * Compose trims the leading above the first line and below the last one by default, so a
         * verse hugs its own text and consecutive verses end up closer together than the lines
         * inside them. Keeping the leading untrimmed gives the chapter one even rhythm, and because
         * the leading is a fraction of the text size it opens up as the reader enlarges the text.
         */
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.None,
        ),
        color = MaterialTheme.colorScheme.onSurface,
    )
    Column(modifier = modifier.alpha(if (isDimmed) DIMMED_ALPHA else 1f)) {
        verse.heading?.let { heading ->
            /*
             * The heading titles the section, not this verse, so it stays outside the tap target —
             * selecting a verse by its heading would pick an arbitrary one. It is left selectable
             * instead, so the section title can be copied through the platform's own control.
             */
            SelectionContainer {
                Text(
                    modifier = Modifier.padding(top = 18.dp, bottom = 2.dp),
                    text = heading,
                    style = textStyle,
                    fontSize = fontSize * HEADING_FONT_SIZE_RATIO,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        /*
         * Both texts align by baseline rather than by top edge: the verse text carries a 1.75 line
         * height, so its first baseline sits well below the top of its box, and any fixed offset on
         * the number would drift the moment the reader changes the text size.
         */
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(vertical = verseVerticalPadding),
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

/**
 * The wash is a span rather than a background on the whole Text so it hugs the words on every line,
 * the way a marker would, instead of painting a block the width of the column.
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
