package com.quare.bibleplanner.core.utils.social

import com.quare.bibleplanner.core.utils.locale.AppLanguage
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage

object SocialUtils {
    private const val INSTAGRAM_DEFAULT = "https://www.instagram.com/bible.planner"
    private const val INSTAGRAM_PT_BR = "$INSTAGRAM_DEFAULT.brasil"
    private const val INSTAGRAM_ES = "$INSTAGRAM_DEFAULT.espanol"

    fun getInstagramUrl(): String = when (getCurrentLanguage()) {
        AppLanguage.PORTUGUESE_BRAZIL -> INSTAGRAM_PT_BR
        AppLanguage.SPANISH -> INSTAGRAM_ES
        AppLanguage.ENGLISH -> INSTAGRAM_DEFAULT
    }
}
