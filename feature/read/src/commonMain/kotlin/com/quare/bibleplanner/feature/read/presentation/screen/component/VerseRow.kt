package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import com.quare.bibleplanner.feature.read.presentation.utils.toBackgroundColor
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import com.quare.bibleplanner.ui.theme.font.toFontFamily

private const val LINE_HEIGHT_RATIO = 1.75f
private const val HEADING_FONT_SIZE_RATIO = 0.94f
private const val VERSE_NUMBER_ALPHA = 0.55f
private const val DIMMED_ALPHA = 0.22f
private val verseNumberFontSize = 12.sp
private val verseNumberWidth = 18.dp

/**
 * A verse and, when the section starts here, its pericope heading. Tapping anywhere on the row
 * selects the verse — the whole row is the target, not just the words.
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
        color = MaterialTheme.colorScheme.onSurface,
    )
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .alpha(if (isDimmed) DIMMED_ALPHA else 1f),
    ) {
        verse.heading?.let { heading ->
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
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                modifier = Modifier
                    .widthIn(min = verseNumberWidth)
                    .padding(top = 6.dp)
                    .alpha(VERSE_NUMBER_ALPHA),
                text = verse.number.toString(),
                fontFamily = displaySerifFontFamily(),
                fontSize = verseNumberFontSize,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
            Text(
                text = verse.text.withHighlight(verse.highlightColor),
                style = textStyle,
                textDecoration = TextDecoration.Underline.takeIf { verse.isSelected },
            )
        }
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
