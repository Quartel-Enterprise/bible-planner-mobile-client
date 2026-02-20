package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.domain.model.VersionModel

internal class VersionMapper {
    fun map(dto: VersionDto): VersionModel = VersionModel(
        id = dto.id,
        name = dto.name,
        language = dto.language,
        country = dto.country,
    )
}
