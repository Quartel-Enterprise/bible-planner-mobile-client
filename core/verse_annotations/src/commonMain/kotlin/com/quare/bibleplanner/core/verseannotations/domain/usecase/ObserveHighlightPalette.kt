package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import kotlinx.coroutines.flow.Flow

fun interface ObserveHighlightPalette {
    operator fun invoke(): Flow<List<HighlightColor.Custom>>
}
