package com.quare.bibleplanner.core.provider.room.di

import androidx.room3.RoomDatabase
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.ChatDao
import com.quare.bibleplanner.core.provider.room.dao.ChatDraftDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.DayStudyDao
import com.quare.bibleplanner.core.provider.room.dao.HighlightPaletteColorDao
import com.quare.bibleplanner.core.provider.room.dao.ProfileDao
import com.quare.bibleplanner.core.provider.room.dao.SavedVerseDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.UserDeviceDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.dao.VerseHighlightDao
import com.quare.bibleplanner.core.provider.room.dao.VerseNoteDao
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val roomModule = module {
    single<AppDatabase> {
        val builder: RoomDatabase.Builder<AppDatabase> = get()
        builder
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single<BookDao> { get<AppDatabase>().bookDao() }
    single<ChapterDao> { get<AppDatabase>().chapterDao() }
    single<VerseDao> { get<AppDatabase>().verseDao() }
    single<DayDao> { get<AppDatabase>().dayDao() }
    single<BibleVersionDao> { get<AppDatabase>().bibleVersionDao() }
    single<SyncedPreferenceDao> { get<AppDatabase>().syncedPreferenceDao() }
    single<DayStudyDao> { get<AppDatabase>().dayStudyDao() }
    single<ChatDao> { get<AppDatabase>().chatDao() }
    single<ChatDraftDao> { get<AppDatabase>().chatDraftDao() }
    single<UserDeviceDao> { get<AppDatabase>().userDeviceDao() }
    single<ProfileDao> { get<AppDatabase>().profileDao() }
    single<VerseHighlightDao> { get<AppDatabase>().verseHighlightDao() }
    single<SavedVerseDao> { get<AppDatabase>().savedVerseDao() }
    single<VerseNoteDao> { get<AppDatabase>().verseNoteDao() }
    single<HighlightPaletteColorDao> { get<AppDatabase>().highlightPaletteColorDao() }
}
