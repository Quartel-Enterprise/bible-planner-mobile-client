package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

object ReaderPaneKey : NavMetadataKey<Boolean>

object VerseSelectionPaneKey : NavMetadataKey<Boolean>

fun getReaderPane(): Map<String, Any> = metadata { put(ReaderPaneKey, true) }

fun getVerseSelectionPane(): Map<String, Any> = metadata { put(VerseSelectionPaneKey, true) }
