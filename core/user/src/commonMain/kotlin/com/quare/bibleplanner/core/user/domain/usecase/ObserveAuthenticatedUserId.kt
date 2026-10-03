package com.quare.bibleplanner.core.user.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveAuthenticatedUserId {
    operator fun invoke(): Flow<String?>
}
