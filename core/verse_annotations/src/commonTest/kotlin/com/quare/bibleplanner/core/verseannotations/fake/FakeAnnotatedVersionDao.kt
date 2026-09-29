package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.provider.room.dao.AnnotatedVersionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAnnotatedVersionDao(
    initialVersionIds: List<String>,
) : AnnotatedVersionDao {
    val versionIds = MutableStateFlow(initialVersionIds)

    override fun getAnnotatedVersionIdsFlow(): Flow<List<String>> = versionIds
}
