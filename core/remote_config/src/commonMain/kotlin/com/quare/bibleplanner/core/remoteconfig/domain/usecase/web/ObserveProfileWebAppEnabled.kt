package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web

import kotlinx.coroutines.flow.Flow

fun interface ObserveProfileWebAppEnabled {
    operator fun invoke(): Flow<Boolean>
}
