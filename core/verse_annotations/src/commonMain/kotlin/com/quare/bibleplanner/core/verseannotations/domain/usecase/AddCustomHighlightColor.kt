package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor

fun interface AddCustomHighlightColor {
    suspend operator fun invoke(color: HighlightColor.Custom)
}
