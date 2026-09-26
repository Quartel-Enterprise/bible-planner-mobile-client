package com.quare.bibleplanner.core.preferences.materialyou.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface GetIsDynamicColorsEnabledFlow {
    operator fun invoke(): Flow<Boolean>
}
