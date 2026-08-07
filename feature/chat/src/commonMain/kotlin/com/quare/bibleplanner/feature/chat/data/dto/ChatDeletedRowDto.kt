package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * All a deletion carries. Postgres cannot check a row policy against a row that no longer exists,
 * so Realtime strips every other column from the old record of a table under RLS — the primary key
 * is the whole payload, however wide the table is.
 */
@Serializable
internal data class ChatDeletedRowDto(
    @SerialName("id") val id: String,
)
