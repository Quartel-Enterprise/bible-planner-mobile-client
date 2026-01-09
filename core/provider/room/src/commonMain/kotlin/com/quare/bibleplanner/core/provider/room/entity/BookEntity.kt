package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String, // BookId enum value as string
    val isRead: Boolean = false,
    val isFavorite: Boolean = false,
)
