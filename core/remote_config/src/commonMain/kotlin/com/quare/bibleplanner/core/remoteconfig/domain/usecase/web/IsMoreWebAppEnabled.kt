package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web

fun interface IsMoreWebAppEnabled {
    suspend operator fun invoke(): Boolean
}
