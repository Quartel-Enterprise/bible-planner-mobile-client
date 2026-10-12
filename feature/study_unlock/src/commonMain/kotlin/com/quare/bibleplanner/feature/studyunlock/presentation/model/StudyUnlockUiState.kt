package com.quare.bibleplanner.feature.studyunlock.presentation.model

import com.quare.bibleplanner.core.model.route.StudyUnlockSurface

internal data class StudyUnlockUiState(
    val surface: StudyUnlockSurface,
    val rewardedRemainingToday: Int,
    val canUnlockWithoutVideo: Boolean,
    val videoState: StudyUnlockVideoState,
) {
    // Why: the sheet shows the free unlock and the ViewModel accepts its click under this same rule.
    val isFreeUnlockShown: Boolean
        get() = canUnlockWithoutVideo && videoState == StudyUnlockVideoState.UNAVAILABLE
}
