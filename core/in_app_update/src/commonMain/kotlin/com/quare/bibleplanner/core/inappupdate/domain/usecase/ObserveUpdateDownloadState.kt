package com.quare.bibleplanner.core.inappupdate.domain.usecase

import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateDownloadState
import kotlinx.coroutines.flow.Flow

fun interface ObserveUpdateDownloadState {
    operator fun invoke(): Flow<UpdateDownloadState>
}
