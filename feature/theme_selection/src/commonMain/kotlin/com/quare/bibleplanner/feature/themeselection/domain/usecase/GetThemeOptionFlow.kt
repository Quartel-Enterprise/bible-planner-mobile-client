package com.quare.bibleplanner.feature.themeselection.domain.usecase

import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow

fun interface GetThemeOptionFlow {
    operator fun invoke(): Flow<Theme>
}
