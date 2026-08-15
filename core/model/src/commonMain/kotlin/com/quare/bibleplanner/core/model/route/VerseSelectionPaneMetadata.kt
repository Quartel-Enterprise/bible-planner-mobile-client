package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

/** Marks the reader entry the selection pane attaches to. */
object ReaderPaneKey : NavMetadataKey<Boolean>

/** Marks the selection entry that renders over — or beside — the reader. */
object VerseSelectionPaneKey : NavMetadataKey<Boolean>

fun readerPane(): Map<String, Any> = metadata { put(ReaderPaneKey, true) }

fun verseSelectionPane(): Map<String, Any> = metadata { put(VerseSelectionPaneKey, true) }
