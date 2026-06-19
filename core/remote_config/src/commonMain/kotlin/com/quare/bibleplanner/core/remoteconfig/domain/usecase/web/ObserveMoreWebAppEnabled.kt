package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web

import kotlinx.coroutines.flow.Flow

fun interface ObserveMoreWebAppEnabled {
    operator fun invoke(): Flow<Boolean>
}
