package com.quare.bibleplanner.core.provider.room.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val roomModule = module {
    single<AppDatabase> {
        val builder: RoomDatabase.Builder<AppDatabase> = get()
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single<BookDao> { get<AppDatabase>().bookDao() }
    single<ChapterDao> { get<AppDatabase>().chapterDao() }
    single<VerseDao> { get<AppDatabase>().verseDao() }
    single<DayDao> { get<AppDatabase>().dayDao() }
    single<BibleVersionDao> { get<AppDatabase>().bibleVersionDao() }
}
