package com.quare.bibleplanner.feature.applanguage.domain.repository

import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow

interface AppLanguageRepository {
    fun getLanguageFlow(): Flow<Language>

    suspend fun setLanguage(language: Language)
}
