package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyCrossReferenceEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyNameEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyOutlineSectionEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.relation.ChapterStudyWithContent

@Dao
interface ChapterStudyDao {
    @Transaction
    @Query("SELECT * FROM chapter_studies WHERE cacheKey = :cacheKey")
    suspend fun getByCacheKey(cacheKey: String): ChapterStudyWithContent?

    @Query("SELECT EXISTS(SELECT 1 FROM chapter_studies WHERE cacheKey = :cacheKey)")
    suspend fun exists(cacheKey: String): Boolean

    @Upsert
    suspend fun upsertStudy(study: ChapterStudyEntity)

    @Query("DELETE FROM chapter_studies WHERE cacheKey = :cacheKey")
    suspend fun deleteStudy(cacheKey: String)

    @Query("DELETE FROM chapter_study_outline_sections WHERE cacheKey = :cacheKey")
    suspend fun deleteOutlineSections(cacheKey: String)

    @Query("DELETE FROM chapter_study_names WHERE cacheKey = :cacheKey")
    suspend fun deleteNames(cacheKey: String)

    @Query("DELETE FROM chapter_study_cross_references WHERE cacheKey = :cacheKey")
    suspend fun deleteCrossReferences(cacheKey: String)

    @Query("DELETE FROM chapter_study_questions WHERE cacheKey = :cacheKey")
    suspend fun deleteQuestions(cacheKey: String)

    @Query("DELETE FROM chapter_study_outline_sections")
    suspend fun deleteAllOutlineSections()

    @Query("DELETE FROM chapter_study_names")
    suspend fun deleteAllNames()

    @Query("DELETE FROM chapter_study_cross_references")
    suspend fun deleteAllCrossReferences()

    @Query("DELETE FROM chapter_study_questions")
    suspend fun deleteAllQuestions()

    @Query("DELETE FROM chapter_studies")
    suspend fun deleteAllStudies()

    @Insert
    suspend fun insertOutlineSections(items: List<ChapterStudyOutlineSectionEntity>)

    @Insert
    suspend fun insertNames(items: List<ChapterStudyNameEntity>)

    @Insert
    suspend fun insertCrossReferences(items: List<ChapterStudyCrossReferenceEntity>)

    @Insert
    suspend fun insertQuestions(items: List<ChapterStudyQuestionEntity>)

    @Transaction
    suspend fun replace(content: ChapterStudyWithContent) {
        val cacheKey = content.study.cacheKey
        upsertStudy(content.study)
        deleteSections(cacheKey)
        insertOutlineSections(content.outlineSections)
        insertNames(content.names)
        insertCrossReferences(content.crossReferences)
        insertQuestions(content.questions)
    }

    @Transaction
    suspend fun deleteByCacheKey(cacheKey: String) {
        deleteSections(cacheKey)
        deleteStudy(cacheKey)
    }

    @Transaction
    suspend fun deleteSections(cacheKey: String) {
        deleteOutlineSections(cacheKey)
        deleteNames(cacheKey)
        deleteCrossReferences(cacheKey)
        deleteQuestions(cacheKey)
    }

    @Transaction
    suspend fun deleteAll() {
        deleteAllOutlineSections()
        deleteAllNames()
        deleteAllCrossReferences()
        deleteAllQuestions()
        deleteAllStudies()
    }
}
