package com.quare.bibleplanner.feature.read.domain.usecase

fun interface SetReaderVerticalReading {
    suspend operator fun invoke(isEnabled: Boolean)
}
