package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVersionModel

fun interface GetListeningVersion {
    suspend operator fun invoke(): ListeningVersionModel?
}
