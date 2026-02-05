package com.quare.bibleplanner.feature.bibleversion.data.repository

import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.feature.bibleversion.data.mapper.BibleVersionMapper
import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersionModel
import com.quare.bibleplanner.feature.bibleversion.domain.model.VersionModel
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionMetadataRepository
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class BibleVersionRepositoryImpl(
    private val bibleVersionDao: BibleVersionDao,
    private val metadataRepository: BibleVersionMetadataRepository,
    private val bibleVersionMapper: BibleVersionMapper,
) : BibleVersionRepository {
    override fun getBibleVersions(): Flow<List<BibleVersionModel>> = flow {
        val supportedVersions = metadataRepository.getVersions().getOrDefault(emptyList())
        emitAll(
            bibleVersionDao.getAllVersionsFlow().map { dataBaseVersions: List<BibleVersionEntity> ->
                supportedVersions.mapNotNull { versionModel: VersionModel ->
                    dataBaseVersions.find { it.id == versionModel.id }?.let { safeEntity: BibleVersionEntity ->
                        bibleVersionMapper.map(
                            entity = safeEntity,
                            versionModel = versionModel,
                        )
                    }
                }
            },
        )
    }
}
