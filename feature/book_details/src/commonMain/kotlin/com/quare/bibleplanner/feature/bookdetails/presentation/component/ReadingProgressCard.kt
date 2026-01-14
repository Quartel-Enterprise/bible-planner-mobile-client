package com.quare.bibleplanner.feature.bookdetails.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.book_details.generated.resources.Res
import bibleplanner.feature.book_details.generated.resources.read_count_suffix
import bibleplanner.feature.book_details.generated.resources.reading_progress
import com.quare.bibleplanner.ui.component.AnimatedIntText
import com.quare.bibleplanner.ui.component.progress.BookProgressBar
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ReadingProgressCard(
    progress: Float,
    readChaptersCount: Int,
    totalChaptersCount: Int,
    bookIdName: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = stringResource(Res.string.reading_progress),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            VerticalSpacer(12)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val suffix = stringResource(Res.string.read_count_suffix)

                    with(sharedTransitionScope) {
                        AnimatedIntText(
                            value = readChaptersCount,
                            label = "reading_progress_numerator_$bookIdName",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "numerator-$bookIdName"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                        )

                        Text(
                            text = " / ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "slash-$bookIdName"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                        )

                        Text(
                            text = totalChaptersCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "denominator-$bookIdName"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                        )

                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    with(sharedTransitionScope) {
                        AnimatedIntText(
                            value = (readChaptersCount * 100 / totalChaptersCount),
                            label = "reading_progress_percentage_$bookIdName",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = "percentage-$bookIdName"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .sharedBounds(
                                    rememberSharedContentState(key = "percentage-symbol-$bookIdName"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                        )
                    }
                }
            }

            VerticalSpacer(24)

            BookProgressBar(
                progress = progress,
                bookIdName = bookIdName,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .fillMaxWidth(),
                height = 14.dp,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            )
        }
    }
}
