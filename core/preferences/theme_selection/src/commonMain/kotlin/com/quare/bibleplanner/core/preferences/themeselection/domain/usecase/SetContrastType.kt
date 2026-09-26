package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase

import com.quare.bibleplanner.core.model.theme.ContrastType

fun interface SetContrastType {
    suspend operator fun invoke(contrastType: ContrastType)
}
