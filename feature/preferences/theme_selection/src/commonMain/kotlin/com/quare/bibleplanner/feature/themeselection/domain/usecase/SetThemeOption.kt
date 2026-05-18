package com.quare.bibleplanner.feature.themeselection.domain.usecase

import com.quare.bibleplanner.ui.theme.model.Theme

fun interface SetThemeOption {
    suspend operator fun invoke(theme: Theme)
}
