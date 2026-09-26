package com.quare.bibleplanner.core.inappupdate.domain.usecase

fun interface CompleteUpdateInstall {
    suspend operator fun invoke()
}
