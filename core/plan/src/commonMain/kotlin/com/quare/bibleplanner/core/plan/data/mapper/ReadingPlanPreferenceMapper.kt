package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

interface ReadingPlanPreferenceMapper {
    fun mapPreferenceToModel(preference: String?): ReadingPlanType

    fun mapModelToPreference(readingPlanType: ReadingPlanType): String
}
