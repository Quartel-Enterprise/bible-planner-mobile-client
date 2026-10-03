package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

object ChapterStudyPaneKey : NavMetadataKey<Boolean>

fun getChapterStudyPane(): Map<String, Any> = metadata { put(ChapterStudyPaneKey, true) }
