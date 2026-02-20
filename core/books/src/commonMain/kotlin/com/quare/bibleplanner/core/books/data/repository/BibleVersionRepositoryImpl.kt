package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.mapper.BibleVersionMapper
import com.quare.bibleplanner.core.books.domain.model.BibleVersionModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionMetadataRepository
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.domain.usecase.getDefaultVersion
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class BibleVersionRepositoryImpl(
    private val bibleVersionDao: BibleVersionDao,
    private val metadataRepository: BibleVersionMetadataRepository,
    private val bibleVersionMapper: BibleVersionMapper,
    private val dataStore: DataStore<Preferences>,
) : BibleVersionRepository {
    private val bibleVersionKey = stringPreferencesKey(BIBLE_VERSION_KEY)

    override fun getBibleVersionsFlow(): Flow<List<BibleVersionModel>> = flow {
        val supportedVersions: List<VersionModel> = metadataRepository.getVersions().getOrDefault(emptyList())
        emitAll(
            combine(
                getSelectedVersionAbbreviationFlow(),
                bibleVersionDao.getAllVersionsFlow().map {
                    bibleVersionMapper.map(
                        dataBaseVersions = it,
                        supportedVersions = supportedVersions,
                    )
                },
            ) { selectedVersion, versions ->
                versions.map { version ->
                    version.copy(
                        isSelected = version.id.equals(
                            other = selectedVersion,
                            ignoreCase = true,
                        ),
                    )
                }
            },
        )
    }

    override fun getSelectedVersionAbbreviationFlow(): Flow<String> = dataStore.data.map { preferences ->
        preferences[bibleVersionKey] ?: getDefaultVersion()
    }

    override suspend fun setSelectedVersionId(id: String) {
        dataStore.edit { preferences ->
            preferences[bibleVersionKey] = id
        }
    }

    companion object {
        private const val BIBLE_VERSION_KEY = "selected_bible_version"
    }
}
