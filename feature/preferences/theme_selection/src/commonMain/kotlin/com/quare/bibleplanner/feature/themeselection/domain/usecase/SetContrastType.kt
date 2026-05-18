package com.quare.bibleplanner.feature.themeselection.domain.usecase

import com.quare.bibleplanner.ui.theme.model.ContrastType

interface SetContrastType {
    suspend operator fun invoke(contrastType: ContrastType)
}
