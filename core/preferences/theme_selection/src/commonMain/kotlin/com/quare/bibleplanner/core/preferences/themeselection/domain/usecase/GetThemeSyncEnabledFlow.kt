package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface GetThemeSyncEnabledFlow {
    operator fun invoke(): Flow<Boolean>
}
