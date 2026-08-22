package com.quare.bibleplanner.feature.read.domain.usecase

fun interface SetReaderRulerLines {
    suspend operator fun invoke(lines: Int)
}
