package com.quare.bibleplanner.feature.bibleversion.data.mapper

import com.quare.bibleplanner.feature.bibleversion.data.dto.VersionDto
import com.quare.bibleplanner.feature.bibleversion.domain.model.VersionModel

internal class VersionMapper {
    fun map(dto: VersionDto): VersionModel = VersionModel(
        id = dto.id,
        name = dto.name,
        language = dto.language,
        country = dto.country,
    )
}
