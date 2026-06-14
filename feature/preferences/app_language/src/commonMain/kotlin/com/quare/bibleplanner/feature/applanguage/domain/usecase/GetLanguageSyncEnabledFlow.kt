package com.quare.bibleplanner.feature.applanguage.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface GetLanguageSyncEnabledFlow {
    operator fun invoke(): Flow<Boolean>
}
