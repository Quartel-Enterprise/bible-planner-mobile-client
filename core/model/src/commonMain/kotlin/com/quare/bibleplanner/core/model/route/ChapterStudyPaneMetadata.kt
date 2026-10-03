package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

/** Marks the chapter study entry that opens beside the reader on a wide window. */
object ChapterStudyPaneKey : NavMetadataKey<Boolean>

fun getChapterStudyPane(): Map<String, Any> = metadata { put(ChapterStudyPaneKey, true) }
