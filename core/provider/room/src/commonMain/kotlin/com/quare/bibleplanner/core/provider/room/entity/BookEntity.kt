package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String, // BookId enum value as string
    @ColumnInfo(defaultValue = "0") val isRead: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    val favoriteUpdatedAt: Long?,
    @ColumnInfo(defaultValue = "0") val isFavoritePendingSync: Boolean,
)
