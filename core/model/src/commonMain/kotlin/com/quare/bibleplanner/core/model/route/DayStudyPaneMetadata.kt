package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

object DayStudyMainPaneKey : NavMetadataKey<Boolean>

object DayStudyDetailPaneKey : NavMetadataKey<Boolean>

fun getDayStudyMainPane(): Map<String, Any> = metadata { put(DayStudyMainPaneKey, true) }

fun getDayStudyDetailPane(): Map<String, Any> = metadata { put(DayStudyDetailPaneKey, true) }
