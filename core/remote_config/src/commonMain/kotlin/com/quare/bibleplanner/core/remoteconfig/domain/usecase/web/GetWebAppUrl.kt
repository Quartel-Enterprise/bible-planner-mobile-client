package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web

fun interface GetWebAppUrl {
    suspend operator fun invoke(): String
}
