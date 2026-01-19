package com.quare.bibleplanner.core.remoteconfig.domain.usecase.login

fun interface IsLoginVisible {
    suspend operator fun invoke(): Boolean
}
