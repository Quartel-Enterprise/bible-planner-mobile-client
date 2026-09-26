package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase

import com.quare.bibleplanner.core.model.theme.Theme

fun interface SetThemeOption {
    suspend operator fun invoke(theme: Theme)
}
