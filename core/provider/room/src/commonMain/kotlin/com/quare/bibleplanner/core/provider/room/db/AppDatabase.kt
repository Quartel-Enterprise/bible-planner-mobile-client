package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.quare.bibleplanner.core.provider.room.converter.BibleVersionDownloadStatusConverter
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.DayStudyDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.UserDeviceDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyChapterSummaryEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyFactEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyTakeawayEntity
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        VerseEntity::class,
        VerseTextEntity::class,
        DayEntity::class,
        BibleVersionEntity::class,
        SyncedPreferenceEntity::class,
        DayStudyEntity::class,
        DayStudyChapterSummaryEntity::class,
        DayStudyTakeawayEntity::class,
        DayStudyFactEntity::class,
        DayStudyQuestionEntity::class,
        UserDeviceEntity::class,
    ],
    version = 11,
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8, spec = Migration7To8Spec::class),
        AutoMigration(from = 8, to = 9, spec = Migration8To9Spec::class),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
    ],
    exportSchema = true,
)
@ColumnTypeConverters(BibleVersionDownloadStatusConverter::class)
@ConstructedBy(DatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    abstract fun chapterDao(): ChapterDao

    abstract fun verseDao(): VerseDao

    abstract fun dayDao(): DayDao

    abstract fun bibleVersionDao(): BibleVersionDao

    abstract fun syncedPreferenceDao(): SyncedPreferenceDao

    abstract fun dayStudyDao(): DayStudyDao

    abstract fun userDeviceDao(): UserDeviceDao
}
