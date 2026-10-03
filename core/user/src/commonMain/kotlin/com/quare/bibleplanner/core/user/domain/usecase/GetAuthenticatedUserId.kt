package com.quare.bibleplanner.core.user.domain.usecase

fun interface GetAuthenticatedUserId {
    suspend operator fun invoke(): String?
}
