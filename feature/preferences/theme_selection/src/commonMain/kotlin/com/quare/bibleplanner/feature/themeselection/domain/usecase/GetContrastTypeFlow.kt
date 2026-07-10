package com.quare.bibleplanner.feature.themeselection.domain.usecase

import com.quare.bibleplanner.ui.theme.model.ContrastType
import kotlinx.coroutines.flow.Flow

fun interface GetContrastTypeFlow {
    operator fun invoke(): Flow<ContrastType>
}
