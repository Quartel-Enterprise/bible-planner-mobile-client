package com.quare.bibleplanner.feature.readingplan.data.mapper

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal class ReadingPlanPreferenceMapperImpl : ReadingPlanPreferenceMapper {
    override fun mapPreferenceToModel(preference: String?): ReadingPlanType = when (preference) {
        CHRONOLOGICAL -> ReadingPlanType.CHRONOLOGICAL
        BOOKS -> ReadingPlanType.BOOKS
        else -> ReadingPlanType.CHRONOLOGICAL
    }

    override fun mapModelToPreference(readingPlanType: ReadingPlanType): String = when (readingPlanType) {
        ReadingPlanType.CHRONOLOGICAL -> CHRONOLOGICAL
        ReadingPlanType.BOOKS -> BOOKS
    }

    companion object {
        private const val CHRONOLOGICAL = "chronological"
        private const val BOOKS = "books"
    }
}
