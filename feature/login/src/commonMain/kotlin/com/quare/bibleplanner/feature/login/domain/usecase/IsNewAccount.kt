package com.quare.bibleplanner.feature.login.domain.usecase

fun interface IsNewAccount {
    suspend operator fun invoke(): Boolean
}
