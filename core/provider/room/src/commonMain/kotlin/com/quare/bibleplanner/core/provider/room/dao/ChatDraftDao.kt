package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDraftDao {
    @Query("SELECT * FROM chat_drafts WHERE threadKey = :threadKey")
    fun observeDraft(threadKey: String): Flow<ChatDraftEntity?>

    @Upsert
    suspend fun upsertDraft(draft: ChatDraftEntity)

    @Query("SELECT * FROM chat_drafts WHERE isPendingSync = 1")
    fun getPendingSyncFlow(): Flow<List<ChatDraftEntity>>

    @Query("SELECT * FROM chat_drafts WHERE isPendingSync = 1")
    suspend fun getPendingSync(): List<ChatDraftEntity>

    @Query(
        "UPDATE chat_drafts SET isPendingSync = 0 " +
            "WHERE threadKey = :threadKey AND updatedAtEpochMillis = :syncedUpdatedAt",
    )
    suspend fun markSynced(
        threadKey: String,
        syncedUpdatedAt: Long,
    )

    /** Applies a remote draft with Last-Write-Wins. Returns the number of rows changed. */
    @Query(
        "UPDATE chat_drafts SET content = :content, updatedAtEpochMillis = :remoteUpdatedAt " +
            "WHERE threadKey = :threadKey AND isPendingSync = 0 AND updatedAtEpochMillis < :remoteUpdatedAt",
    )
    suspend fun applyRemoteDraft(
        threadKey: String,
        content: String,
        remoteUpdatedAt: Long,
    ): Int

    @Query("SELECT * FROM chat_drafts WHERE threadKey = :threadKey")
    suspend fun getDraft(threadKey: String): ChatDraftEntity?

    @Query("DELETE FROM chat_drafts")
    suspend fun deleteAll()
}
