package com.quare.bibleplanner.feature.materialyou.domain.repository

import kotlinx.coroutines.flow.Flow

interface MaterialYouRepository {
    fun getIsDynamicColorsEnabledFlow(): Flow<Boolean>

    suspend fun setIsDynamicColorsEnabled(isEnabled: Boolean)
}
