package com.quare.bibleplanner.feature.onboardingstartdate.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingStartDateRepository {
    fun getDontShowAgainFlow(): Flow<Boolean>
    suspend fun setDontShowAgain(dontShow: Boolean)
}

