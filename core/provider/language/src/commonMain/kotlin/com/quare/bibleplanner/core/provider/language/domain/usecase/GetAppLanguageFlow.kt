package com.quare.bibleplanner.core.provider.language.domain.usecase

import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow

fun interface GetAppLanguageFlow {
    operator fun invoke(): Flow<Language>
}
