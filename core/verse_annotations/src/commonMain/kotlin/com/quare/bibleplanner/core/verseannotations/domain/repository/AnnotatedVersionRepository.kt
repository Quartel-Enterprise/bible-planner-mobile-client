package com.quare.bibleplanner.core.verseannotations.domain.repository

import kotlinx.coroutines.flow.Flow

fun interface AnnotatedVersionRepository {
    fun observeAnnotatedVersionIds(): Flow<List<String>>
}
