package com.quare.bibleplanner.android.designsystem.language

import androidx.annotation.StringRes
import com.quare.bibleplanner.android.designsystem.R
import com.quare.bibleplanner.core.utils.locale.Language

// Why: the language picker and the Bible version list name languages the same way.
@StringRes
fun Language.toNameRes(): Int = when (this) {
    Language.ENGLISH -> R.string.language_name_english
    Language.PORTUGUESE_BRAZIL -> R.string.language_name_portuguese_brazil
    Language.SPANISH -> R.string.language_name_spanish
}
