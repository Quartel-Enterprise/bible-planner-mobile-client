package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.provider.room.dao.AnnotatedVersionDao
import com.quare.bibleplanner.core.verseannotations.domain.repository.AnnotatedVersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class AnnotatedVersionRepositoryImpl(
    private val annotatedVersionDao: AnnotatedVersionDao,
) : AnnotatedVersionRepository {
    override fun observeAnnotatedVersionIds(): Flow<List<String>> = annotatedVersionDao
        .getAnnotatedVersionIdsFlow()
        .map(List<String>::sorted)
        .distinctUntilChanged()
}
