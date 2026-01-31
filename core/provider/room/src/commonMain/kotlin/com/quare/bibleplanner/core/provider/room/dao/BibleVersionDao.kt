package com.quare.bibleplanner.core.provider.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionDownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleVersionDao {
    @Query("SELECT * FROM bible_versions")
    fun getAllVersionsFlow(): Flow<List<BibleVersionEntity>>

    @Query("SELECT * FROM bible_versions")
    suspend fun getAllVersions(): List<BibleVersionEntity>

    @Query("SELECT * FROM bible_versions WHERE id = :id")
    suspend fun getVersionById(id: String): BibleVersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: BibleVersionEntity)

    @Update
    suspend fun updateVersion(version: BibleVersionEntity)

    @Query("UPDATE bible_versions SET downloadProgress = :progress WHERE id = :id")
    suspend fun updateDownloadProgress(
        id: String,
        progress: Float,
    )

    @Query("UPDATE bible_versions SET status = :status WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: BibleVersionDownloadStatus,
    )

    @Query("DELETE FROM bible_versions WHERE id = :id")
    suspend fun deleteVersion(id: String)
}
