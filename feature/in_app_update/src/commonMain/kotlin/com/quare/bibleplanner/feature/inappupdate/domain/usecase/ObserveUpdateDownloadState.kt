package com.quare.bibleplanner.feature.inappupdate.domain.usecase

import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateDownloadState
import kotlinx.coroutines.flow.Flow

fun interface ObserveUpdateDownloadState {
    operator fun invoke(): Flow<UpdateDownloadState>
}
