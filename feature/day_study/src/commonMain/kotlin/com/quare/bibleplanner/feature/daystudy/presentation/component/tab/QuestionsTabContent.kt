package com.quare.bibleplanner.feature.daystudy.presentation.component.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_questions_title
import com.quare.bibleplanner.feature.daystudy.domain.model.QaModel
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudyExpandableCard
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuestionsTabContent(questions: List<QaModel>) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.ai_questions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        VerticalSpacer(12)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            questions.forEach { question ->
                DayStudyExpandableCard(
                    title = question.question,
                    body = question.answer,
                    initiallyExpanded = false,
                )
            }
        }
    }
}
