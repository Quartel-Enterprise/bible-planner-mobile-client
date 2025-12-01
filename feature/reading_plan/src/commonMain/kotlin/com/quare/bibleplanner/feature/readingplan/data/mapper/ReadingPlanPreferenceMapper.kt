package com.quare.bibleplanner.feature.readingplan.data.mapper

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

interface ReadingPlanPreferenceMapper {
    fun mapPreferenceToModel(preference: String?): ReadingPlanType

    fun mapModelToPreference(readingPlanType: ReadingPlanType): String
}
