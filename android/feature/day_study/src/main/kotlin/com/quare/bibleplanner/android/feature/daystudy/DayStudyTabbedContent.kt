package com.quare.bibleplanner.android.feature.daystudy

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.study.AiDisclaimerText
import com.quare.bibleplanner.android.designsystem.study.ExpandableText
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.daystudy.domain.model.FactModel
import com.quare.bibleplanner.core.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.core.daystudy.domain.model.QaModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyTab
import kotlinx.coroutines.launch

private val contentMaxWidth = 720.dp
private val tabContentPadding = PaddingValues(
    start = 20.dp,
    top = 10.dp,
    end = 20.dp,
    bottom = 24.dp,
)
private const val EXPANDED_ARROW_DEGREES = 180f

@get:StringRes
private val DayStudyTab.titleRes: Int
    get() = when (this) {
        DayStudyTab.SUMMARY -> R.string.day_study_tab_summary
        DayStudyTab.CONTEXT -> R.string.day_study_tab_context
        DayStudyTab.QUESTIONS -> R.string.day_study_tab_questions
    }

@Composable
internal fun DayStudyTabbedContent(
    study: DayStudyModel,
    showHeader: Boolean,
    onAskAiClick: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { DayStudyTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showHeader) {
            DayStudyHeader(
                passageLabel = study.passageLabel,
                showIcon = true,
                modifier = Modifier
                    .widthIn(max = contentMaxWidth)
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        top = 16.dp,
                        end = 20.dp,
                        bottom = 12.dp,
                    ),
            )
        }
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .widthIn(max = contentMaxWidth)
                .fillMaxWidth(),
        ) {
            DayStudyTab.entries.forEach { tab ->
                Tab(
                    selected = tab.ordinal == pagerState.currentPage,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(tab.ordinal) } },
                    text = { Text(text = stringResource(tab.titleRes)) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            beyondViewportPageCount = DayStudyTab.entries.size,
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = contentMaxWidth)
                        .fillMaxWidth()
                        .padding(tabContentPadding),
                ) {
                    when (DayStudyTab.entries[page]) {
                        DayStudyTab.SUMMARY -> SummaryTabContent(study)

                        DayStudyTab.CONTEXT -> ContextTabContent(study.context)

                        DayStudyTab.QUESTIONS -> QuestionsTabContent(
                            questions = study.commonQuestions,
                            passageLabel = study.passageLabel,
                            onAskAiClick = onAskAiClick,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    AiDisclaimerText()
                }
            }
        }
    }
}

@Composable
private fun SummaryTabContent(study: DayStudyModel) {
    ExpandableText(text = study.overview)
    Spacer(modifier = Modifier.height(18.dp))
    Text(
        text = stringResource(R.string.day_study_summary_by_chapter),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        study.chapterSummaries.forEachIndexed { index, chapterSummary ->
            ExpandableCard(
                title = chapterSummary.title,
                body = chapterSummary.body,
                initiallyExpanded = index == 0,
            )
        }
    }
    Spacer(modifier = Modifier.height(22.dp))
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
                text = stringResource(R.string.day_study_summary_takeaways),
                modifier = Modifier.padding(bottom = 2.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            study.takeaways.forEach { takeaway ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

@Composable
private fun ContextTabContent(context: HistoricalContextModel) {
    TabTitleRow(
        icon = Icons.Rounded.HistoryEdu,
        title = stringResource(R.string.day_study_context_title),
    )
    Spacer(modifier = Modifier.height(12.dp))
    ExpandableText(text = context.body)
    Spacer(modifier = Modifier.height(16.dp))
    FactsSurface(facts = context.facts)
}

@Composable
private fun FactsSurface(facts: List<FactModel>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            facts.forEach { fact ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = fact.label,
                        modifier = Modifier
                            .widthIn(min = 62.dp)
                            .padding(top = 1.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = fact.value,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionsTabContent(
    questions: List<QaModel>,
    passageLabel: String,
    onAskAiClick: () -> Unit,
) {
    TabTitleRow(
        icon = Icons.AutoMirrored.Rounded.HelpOutline,
        title = stringResource(R.string.day_study_questions_title),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        questions.forEach { question ->
            ExpandableCard(
                title = question.question,
                body = question.answer,
                initiallyExpanded = false,
            )
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    AskAboutReadingCard(
        passageLabel = passageLabel,
        onClick = onAskAiClick,
    )
}

@Composable
private fun TabTitleRow(
    icon: ImageVector,
    title: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ExpandableCard(
    title: String,
    body: String,
    initiallyExpanded: Boolean,
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) EXPANDED_ARROW_DEGREES else 0f,
        label = "expandable_card_arrow",
    )
    Surface(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 14.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = arrowRotation },
                )
            }
            AnimatedVisibility(isExpanded) {
                Text(
                    text = body,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AskAboutReadingCard(
    passageLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.day_study_chat_entry_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                    ).padding(
                        horizontal = 14.dp,
                        vertical = 11.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.day_study_chat_entry_placeholder, passageLabel),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.day_study_chat_entry_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
