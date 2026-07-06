package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.DayStudyChapterSummaryEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyFactEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyTakeawayEntity
import com.quare.bibleplanner.core.provider.room.relation.DayStudyWithContent

@Dao
interface DayStudyDao {
    @Transaction
    @Query("SELECT * FROM day_studies WHERE cacheKey = :cacheKey")
    suspend fun getByCacheKey(cacheKey: String): DayStudyWithContent?

    @Upsert
    suspend fun upsertStudy(study: DayStudyEntity)

    @Query("DELETE FROM day_studies WHERE cacheKey = :cacheKey")
    suspend fun deleteStudy(cacheKey: String)

    @Query("DELETE FROM day_study_chapter_summaries WHERE cacheKey = :cacheKey")
    suspend fun deleteChapterSummaries(cacheKey: String)

    @Query("DELETE FROM day_study_takeaways WHERE cacheKey = :cacheKey")
    suspend fun deleteTakeaways(cacheKey: String)

    @Query("DELETE FROM day_study_facts WHERE cacheKey = :cacheKey")
    suspend fun deleteFacts(cacheKey: String)

    @Query("DELETE FROM day_study_questions WHERE cacheKey = :cacheKey")
    suspend fun deleteQuestions(cacheKey: String)

    @Query("DELETE FROM day_study_chapter_summaries")
    suspend fun deleteAllChapterSummaries()

    @Query("DELETE FROM day_study_takeaways")
    suspend fun deleteAllTakeaways()

    @Query("DELETE FROM day_study_facts")
    suspend fun deleteAllFacts()

    @Query("DELETE FROM day_study_questions")
    suspend fun deleteAllQuestions()

    @Query("DELETE FROM day_studies")
    suspend fun deleteAllStudies()

    @Insert
    suspend fun insertChapterSummaries(items: List<DayStudyChapterSummaryEntity>)

    @Insert
    suspend fun insertTakeaways(items: List<DayStudyTakeawayEntity>)

    @Insert
    suspend fun insertFacts(items: List<DayStudyFactEntity>)

    @Insert
    suspend fun insertQuestions(items: List<DayStudyQuestionEntity>)

    @Transaction
    suspend fun replace(content: DayStudyWithContent) {
        val cacheKey = content.study.cacheKey
        upsertStudy(content.study)
        deleteChapterSummaries(cacheKey)
        deleteTakeaways(cacheKey)
        deleteFacts(cacheKey)
        deleteQuestions(cacheKey)
        insertChapterSummaries(content.chapterSummaries)
        insertTakeaways(content.takeaways)
        insertFacts(content.facts)
        insertQuestions(content.questions)
    }

    @Transaction
    suspend fun deleteByCacheKey(cacheKey: String) {
        deleteChapterSummaries(cacheKey)
        deleteTakeaways(cacheKey)
        deleteFacts(cacheKey)
        deleteQuestions(cacheKey)
        deleteStudy(cacheKey)
    }

    @Transaction
    suspend fun deleteAll() {
        deleteAllChapterSummaries()
        deleteAllTakeaways()
        deleteAllFacts()
        deleteAllQuestions()
        deleteAllStudies()
    }
}
