package com.quare.bibleplanner.feature.daystudy.presentation.component.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_summary_by_chapter
import bibleplanner.feature.day_study.generated.resources.ai_summary_takeaways
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudyExpandableCard
import com.quare.bibleplanner.ui.component.ExpandableText
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SummaryTabContent(study: DayStudyModel) {
    Column {
        ExpandableText(
            text = study.overview,
            style = MaterialTheme.typography.bodyLarge,
        )
        VerticalSpacer(18)
        Text(
            text = stringResource(Res.string.ai_summary_by_chapter),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalSpacer(10)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            study.chapterSummaries.forEachIndexed { index, chapterSummary ->
                DayStudyExpandableCard(
                    title = chapterSummary.title,
                    body = chapterSummary.body,
                    initiallyExpanded = index == 0,
                )
            }
        }
        VerticalSpacer(22)
        TakeawaysBlock(takeaways = study.takeaways)
    }
}

@Composable
private fun TakeawaysBlock(takeaways: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.ai_summary_takeaways),
                modifier = Modifier.padding(bottom = 2.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            takeaways.forEach { takeaway ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = takeaway,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
