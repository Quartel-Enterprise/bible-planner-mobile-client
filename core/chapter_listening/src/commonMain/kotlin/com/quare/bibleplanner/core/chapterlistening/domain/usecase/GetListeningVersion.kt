package com.quare.bibleplanner.core.chapterlistening.domain.usecase

fun interface GetListeningVersion {
    suspend operator fun invoke(): String
}
