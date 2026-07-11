package com.quare.bibleplanner.feature.inappupdate.domain.usecase

fun interface CompleteUpdateInstall {
    suspend operator fun invoke()
}
