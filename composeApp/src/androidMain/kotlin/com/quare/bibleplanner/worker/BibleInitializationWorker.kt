package com.quare.bibleplanner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.notification.BibleInitializationNotifier
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BibleInitializationWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val booksRepository: BooksRepository by inject()
    private val notifier = BibleInitializationNotifier(applicationContext)

    override suspend fun doWork(): Result {
        if (booksRepository.getBooks().isNotEmpty()) {
            return Result.success()
        }

        notifier.initialize()
        setForeground(buildForegroundInfo(0, 0))

        return try {
            booksRepository.initializeDatabase { current, total ->
                setForegroundAsync(buildForegroundInfo(current, total))
            }
            notifier.dismiss()
            notifier.showComplete()
            Result.success()
        } catch (_: Exception) {
            notifier.dismiss()
            notifier.showError()
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private fun buildForegroundInfo(current: Int, total: Int) = ForegroundInfo(
        BibleInitializationNotifier.NOTIFICATION_ID,
        notifier.buildProgressNotification(current, total),
    )

    companion object {
        const val WORK_NAME = "bible_initialization_work"
        private const val MAX_RETRIES = 2
    }
}
