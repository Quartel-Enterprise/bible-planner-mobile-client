package com.quare.bibleplanner.feature.applanguage.domain

import com.quare.bibleplanner.core.utils.locale.Language

fun interface ApplyLocale {
    operator fun invoke(language: Language)
}
