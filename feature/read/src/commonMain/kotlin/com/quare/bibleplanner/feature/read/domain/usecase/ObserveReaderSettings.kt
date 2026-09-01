package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import kotlinx.coroutines.flow.Flow

fun interface ObserveReaderSettings {
    operator fun invoke(): Flow<ReaderSettingsModel>
}
