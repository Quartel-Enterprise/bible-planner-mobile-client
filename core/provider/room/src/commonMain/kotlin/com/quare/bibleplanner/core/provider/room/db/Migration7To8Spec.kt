package com.quare.bibleplanner.core.provider.room.db

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(tableName = "bible_versions", columnName = "downloadProgress")
class Migration7To8Spec : AutoMigrationSpec
