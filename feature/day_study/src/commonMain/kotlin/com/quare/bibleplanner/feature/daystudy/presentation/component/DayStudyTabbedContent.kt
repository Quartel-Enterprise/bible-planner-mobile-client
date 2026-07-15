package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.presentation.component.tab.ContextTabContent
import com.quare.bibleplanner.feature.daystudy.presentation.component.tab.QuestionsTabContent
import com.quare.bibleplanner.feature.daystudy.presentation.component.tab.SummaryTabContent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyTab
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import kotlinx.coroutines.launch

@Composable
internal fun DayStudyTabbedContent(
    study: DayStudyModel,
    contentMaxWidth: Dp,
    modifier: Modifier = Modifier,
    tabRowModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val pagerState = rememberPagerState(pageCount = { DayStudyTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val selectedTab by remember {
        derivedStateOf { DayStudyTab.entries[pagerState.currentPage] }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DayStudyTabRow(
            selectedTab = selectedTab,
            onSelectTab = { tab ->
                coroutineScope.launch { pagerState.animateScrollToPage(tab.ordinal) }
            },
            modifier = Modifier
                .widthIn(max = contentMaxWidth)
                .fillMaxWidth()
                .then(tabRowModifier),
        )
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
                        .padding(contentPadding),
                ) {
                    when (DayStudyTab.entries[page]) {
                        DayStudyTab.SUMMARY -> SummaryTabContent(study)
                        DayStudyTab.CONTEXT -> ContextTabContent(study.context)
                        DayStudyTab.QUESTIONS -> QuestionsTabContent(study.commonQuestions)
                    }
                    VerticalSpacer(20)
                    DayStudyDisclaimer()
                }
            }
        }
    }
}
