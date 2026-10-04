package com.quare.bibleplanner.feature.studyunlock.presentation.model

import com.quare.bibleplanner.core.model.route.StudyUnlockSurface

internal data class StudyUnlockUiState(
    val surface: StudyUnlockSurface,
    val rewardedRemainingToday: Int,
    val videoState: StudyUnlockVideoState,
)
