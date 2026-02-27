package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.utils.locale.Language

internal class VersionMapper {
    fun map(dto: VersionDto): VersionModel = VersionModel(
        id = dto.id,
        name = dto.name,
        language = when (dto.language) {
            "pt" -> Language.PORTUGUESE_BRAZIL
            "es" -> Language.SPANISH
            else -> Language.ENGLISH
        },
    )
}
