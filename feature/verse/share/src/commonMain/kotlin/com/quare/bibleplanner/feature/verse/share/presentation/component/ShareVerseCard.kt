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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.app_name
import bibleplanner.feature.verse.share.generated.resources.bible_planner_logo
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val CARD_ASPECT_RATIO = 0.59f
private const val QUOTE_MAX_LINES = 11
private const val QUOTE_LINE_HEIGHT_RATIO = 1.62f
private val quoteFontSize = 16.sp
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "“",
                fontFamily = uiState.font.toFontFamily(),
                fontSize = quoteFontSize * 2.8f,
                color = background.subtitleColor,
            )
            Text(
                text = uiState.quote,
                fontFamily = uiState.font.toFontFamily(),
                fontSize = quoteFontSize,
                lineHeight = quoteFontSize * QUOTE_LINE_HEIGHT_RATIO,
                fontStyle = FontStyle.Italic,
                color = background.textColor,
                maxLines = QUOTE_MAX_LINES,
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
