package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    // Why: stores BookId.name, so renaming a BookId entry breaks rows already persisted and synced.
    @PrimaryKey
    val id: String,
    @ColumnInfo(defaultValue = "0") val isRead: Boolean,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean,
    val favoriteUpdatedAt: Long?,
    @ColumnInfo(defaultValue = "0") val isFavoritePendingSync: Boolean,
)
