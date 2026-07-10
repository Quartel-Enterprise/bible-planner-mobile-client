package com.quare.bibleplanner.feature.themeselection.domain.usecase

import com.quare.bibleplanner.ui.theme.model.ContrastType

fun interface SetContrastType {
    suspend operator fun invoke(contrastType: ContrastType)
}
