package com.quare.bibleplanner.feature.verse.share.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.app_name
import bibleplanner.feature.verse.share.generated.resources.bible_planner_logo
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val CARD_ASPECT_RATIO = 0.59f
private const val QUOTE_LINE_HEIGHT_RATIO = 1.62f
private const val QUOTE_MARK_RATIO = 2.8f
private const val QUOTE_MARK_LINE_HEIGHT_RATIO = 0.7f
private val quoteFontSize = 16.sp
private val minQuoteFontSize = 7.sp
private val quoteFontStep = 0.5.sp
private val referenceFontSize = 10.5.sp
private val referenceLetterSpacing = 1.6.sp
private val logoSize = 22.dp

/**
 * The card as it will be shared. It is a plain composable so the same tree serves both the preview
 * and the capture, which is what guarantees the image matches what the user approved.
 */
@Composable
internal fun ShareVerseCard(
    uiState: ShareVerseUiState,
    modifier: Modifier = Modifier,
) {
    val background = uiState.background
    Column(
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(listOf(background.startColor, background.endColor)),
            ).padding(26.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            /*
             * The mark is set on a line shorter than itself, the way the design draws it: at nearly
             * three times the body size its own leading would otherwise eat the room the passage
             * needs.
             */
            Text(
                text = "“",
                fontFamily = uiState.font.toFontFamily(),
                fontSize = quoteFontSize * QUOTE_MARK_RATIO,
                lineHeight = QUOTE_MARK_LINE_HEIGHT_RATIO.em,
                color = background.subtitleColor,
            )
            /*
             * The card is a fixed shape, so the passage has to meet it rather than the other way
             * round: the type shrinks step by step until the whole selection fits the space left
             * between the quote mark and the reference. A long passage lands small, but whole.
             */
            BasicText(
                modifier = Modifier.weight(1f),
                text = uiState.quote,
                style = TextStyle(
                    fontFamily = uiState.font.toFontFamily(),
                    /*
                     * Relative to the type rather than a fixed size: an absolute line height would
                     * hold every line as tall as it started, so shrinking the glyphs would never
                     * shorten the block enough to fit.
                     */
                    lineHeight = QUOTE_LINE_HEIGHT_RATIO.em,
                    fontStyle = FontStyle.Italic,
                    color = background.textColor,
                ),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = minQuoteFontSize,
                    maxFontSize = quoteFontSize,
                    stepSize = quoteFontStep,
                ),
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${uiState.reference.uppercase()} · ${uiState.versionAbbreviation}",
                fontSize = referenceFontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = referenceLetterSpacing,
                color = background.subtitleColor,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                modifier = Modifier.size(logoSize).clip(RoundedCornerShape(6.dp)),
                painter = painterResource(Res.drawable.bible_planner_logo),
                contentDescription = null,
            )
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = background.textColor,
            )
        }
    }
}
