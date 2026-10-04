package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

object SheetPaneKey : NavMetadataKey<Boolean>

fun getSheetPane(): Map<String, Any> = metadata { put(SheetPaneKey, true) }
