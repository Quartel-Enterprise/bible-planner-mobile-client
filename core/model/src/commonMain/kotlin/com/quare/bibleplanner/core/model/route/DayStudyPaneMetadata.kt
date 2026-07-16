package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

object DayStudyMainPaneKey : NavMetadataKey<Boolean>

object DayStudyDetailPaneKey : NavMetadataKey<Boolean>

fun dayStudyMainPane(): Map<String, Any> = metadata { put(DayStudyMainPaneKey, true) }

fun dayStudyDetailPane(): Map<String, Any> = metadata { put(DayStudyDetailPaneKey, true) }
