package com.quare.bibleplanner.feature.onboardingstartdate.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.model.OnboardingStartDateUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingStartBottomSheet(
    isDontShowAgainMarked: Boolean,
    onEvent: (OnboardingStartDateUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = {
            onEvent(OnboardingStartDateUiEvent.OnDismiss)
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SetDateHeader(
                onCloseClick = {
                    onEvent(OnboardingStartDateUiEvent.OnDismiss)
                },
            )

            SetDateOnboardingDescription(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            EditDateInfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            DontShowAgainRow(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onEvent(
                        OnboardingStartDateUiEvent.OnDontShowAgainClick,
                    )
                },
                isDontShowAgainMarked = isDontShowAgainMarked,
            )

            ActionButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onEvent = onEvent,
            )
        }
    }
}
