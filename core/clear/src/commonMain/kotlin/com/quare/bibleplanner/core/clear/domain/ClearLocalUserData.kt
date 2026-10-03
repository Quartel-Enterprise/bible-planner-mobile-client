package com.quare.bibleplanner.core.clear.domain

fun interface ClearLocalUserData {
    suspend operator fun invoke()
}
