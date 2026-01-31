package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.BibleVersion
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionDownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

class InitializeBibleVersionsUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val supabaseClient: SupabaseClient,
) {
    suspend operator fun invoke() {
        val availableInSupabase = runCatching {
            supabaseClient.storage
                .from("content")
                .list("bible")
                .map { it.name }
        }.getOrNull().orEmpty()

        val knownVersions = BibleVersion.entries.map { bibleVersion ->
            BibleVersionEntity(
                id = bibleVersion.name,
                downloadProgress = 0f,
                status = BibleVersionDownloadStatus.NOT_STARTED,
            )
        }

        knownVersions.forEach { version ->
            val folderExists = availableInSupabase.any { it.equals(version.id, ignoreCase = true) }
            if (folderExists) {
                val existingVersion = bibleVersionDao.getVersionById(version.id)
                if (existingVersion == null) {
                    bibleVersionDao.insertVersion(version)
                }
            }
        }
    }
}
