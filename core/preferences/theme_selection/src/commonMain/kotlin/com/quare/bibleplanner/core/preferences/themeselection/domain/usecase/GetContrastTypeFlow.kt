package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase

import com.quare.bibleplanner.core.model.theme.ContrastType
import kotlinx.coroutines.flow.Flow

fun interface GetContrastTypeFlow {
    operator fun invoke(): Flow<ContrastType>
}
