package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.DeleteColumn
import androidx.room3.migration.AutoMigrationSpec

@DeleteColumn(tableName = "bible_versions", columnName = "downloadProgress")
class Migration7To8Spec : AutoMigrationSpec
