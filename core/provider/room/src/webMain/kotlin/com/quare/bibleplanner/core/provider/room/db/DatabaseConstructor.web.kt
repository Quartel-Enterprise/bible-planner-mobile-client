package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.quare.bibleplanner.core.provider.room.utils.DatabaseUtils
import org.w3c.dom.Worker

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> = Room
    .databaseBuilder<AppDatabase>(name = DatabaseUtils.PATH)
    .setDriver(WebWorkerSQLiteDriver(createSqliteWorker()))

@OptIn(ExperimentalWasmJsInterop::class)
private fun createSqliteWorker(): Worker =
    js("""new Worker(new URL("bibleplanner-sqlite-worker/worker.js", import.meta.url), { type: "module" })""")
