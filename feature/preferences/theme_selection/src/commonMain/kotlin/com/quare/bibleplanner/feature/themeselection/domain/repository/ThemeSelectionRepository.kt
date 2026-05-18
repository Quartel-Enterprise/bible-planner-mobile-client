package com.quare.bibleplanner.feature.themeselection.domain.repository

import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeSelectionRepository {
    fun getThemeFlow(): Flow<Theme>

    suspend fun setTheme(theme: Theme)

    fun getContrastTypeFlow(): Flow<ContrastType>

    suspend fun setContrastType(contrastType: ContrastType)
}
