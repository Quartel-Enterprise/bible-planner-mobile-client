package com.quare.bibleplanner.android.feature.bookdetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.utils.transition.TransitionScopes
import com.quare.bibleplanner.ui.utils.transition.sharedBoundsOf
import com.quare.bibleplanner.ui.utils.transition.sharedElementOf

private const val COUNT_ANIMATION_MILLIS = 300

@Composable
internal fun BookDetailsProgressCard(
    progress: Float,
    readChaptersCount: Int,
    totalChaptersCount: Int,
    bookIdName: String,
    transitionScopes: TransitionScopes?,
) {
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.book_details_reading_progress),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedCountText(
                        value = readChaptersCount,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.sharedElementOf(
                            transitionScopes = transitionScopes,
                            key = "numerator-$bookIdName",
                        ),
                    )
                    Text(
                        text = " / ",
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryTextColor,
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .sharedElementOf(
                                transitionScopes = transitionScopes,
                                key = "slash-$bookIdName",
                            ),
                    )
                    Text(
                        text = totalChaptersCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryTextColor,
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .sharedElementOf(
                                transitionScopes = transitionScopes,
                                key = "denominator-$bookIdName",
                            ),
                    )
                    Text(
                        text = " ${stringResource(R.string.book_details_read_count_suffix)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(
                            start = 4.dp,
                            bottom = 2.dp,
                        ),
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedCountText(
                        value = if (totalChaptersCount > 0) readChaptersCount * 100 / totalChaptersCount else 0,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.sharedBoundsOf(
                            transitionScopes = transitionScopes,
                            key = "percentage-$bookIdName",
                        ),
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.sharedBoundsOf(
                            transitionScopes = transitionScopes,
                            key = "percentage-symbol-$bookIdName",
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(CircleShape)
                    .sharedBoundsOf(
                        transitionScopes = transitionScopes,
                        key = "progress-bar-$bookIdName",
                    ),
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                strokeCap = StrokeCap.Round,
                // Why: a negative gap makes the track run under the rounded indicator, as on the books list's bar.
                gapSize = (-15).dp,
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun AnimatedCountText(
    value: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            val enter = fadeIn(tween(COUNT_ANIMATION_MILLIS)) +
                slideInVertically(tween(COUNT_ANIMATION_MILLIS)) { height -> height }
            val exit = fadeOut(tween(COUNT_ANIMATION_MILLIS)) +
                slideOutVertically(tween(COUNT_ANIMATION_MILLIS)) { height -> -height }
            enter togetherWith exit
        },
        modifier = modifier,
        label = "book_details_count",
    ) { count ->
        Text(
            text = count.toString(),
            style = style,
        )
    }
}
