package com.quare.bibleplanner.core.inappupdate.domain.usecase

fun interface RequestUpdatePromptIfNeeded {
    suspend operator fun invoke()
}
