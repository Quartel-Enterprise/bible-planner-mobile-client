package com.quare.bibleplanner.feature.inappupdate.domain.usecase

fun interface RequestUpdatePromptIfNeeded {
    suspend operator fun invoke()
}
