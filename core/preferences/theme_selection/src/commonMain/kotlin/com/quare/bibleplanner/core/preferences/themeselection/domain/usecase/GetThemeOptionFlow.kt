package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase

import com.quare.bibleplanner.core.model.theme.Theme
import kotlinx.coroutines.flow.Flow

fun interface GetThemeOptionFlow {
    operator fun invoke(): Flow<Theme>
}
