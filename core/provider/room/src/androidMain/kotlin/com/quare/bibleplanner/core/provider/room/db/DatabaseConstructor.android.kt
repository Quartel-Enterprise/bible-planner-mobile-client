package com.quare.bibleplanner.core.provider.room.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.quare.bibleplanner.core.provider.room.utils.DatabaseUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

/**
 * Room takes a `ReentrantLock` before syncing its invalidation triggers and releases it after the
 * trigger transaction, which suspends in between: `ObservedTableStates.onSync` is an `inline`
 * function whose body is inlined into the suspending `syncTriggers` (androidx.room3 3.0.2). On a
 * multi-threaded dispatcher the coroutine can resume on a different thread, and the release then
 * throws `IllegalMonitorStateException` — a crash that fires whenever a screen starts or stops
 * collecting a DAO `Flow`. Running Room's queries on a single thread keeps every lock/unlock pair
 * on the same thread.
 *
 * Android pays nothing for this: [AndroidSQLiteDriver] brings its own pool, so Room funnels every
 * statement through a single connection anyway. iOS and the desktop use the bundled driver with a
 * real multi-connection pool, so they stay on `Dispatchers.IO` and keep their read parallelism.
 *
 * Reported upstream against androidx Room; drop this once a fixed version ships.
 */
@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
private val queryContext: CoroutineContext = newSingleThreadContext(name = "room-query")

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(DatabaseUtils.PATH)
    return Room
        .databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath,
        ).setDriver(AndroidSQLiteDriver())
        .setQueryCoroutineContext(queryContext)
}
