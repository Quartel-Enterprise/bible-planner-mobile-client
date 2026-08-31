package com.quare.bibleplanner.feature.studysuggestion.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface GetStudySuggestionSyncEnabledFlow {
    operator fun invoke(): Flow<Boolean>
}
