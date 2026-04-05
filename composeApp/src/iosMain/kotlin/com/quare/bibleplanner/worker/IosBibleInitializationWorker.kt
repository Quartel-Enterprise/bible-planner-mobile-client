package com.quare.bibleplanner.worker

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.notification.IosBibleInitializationNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Background worker for iOS that initializes the Bible database and shows
 * progress via local notifications. Exposed to Swift so it can be called
 * from BGTaskScheduler.
 */
@Suppress("unused") // Called from Swift
class IosBibleInitializationWorker : KoinComponent {

    private val booksRepository: BooksRepository by inject()
    private val notifier = IosBibleInitializationNotifier()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Starts the initialization. Calls [onComplete] with `true` on success,
     * `false` on failure. Safe to call from Swift BGProcessingTask handler.
     */
    fun start(onComplete: (Boolean) -> Unit) {
        scope.launch {
            try {
                if (booksRepository.getBooks().isEmpty()) {
                    notifier.showProgress(0, 0)
                    booksRepository.initializeDatabase { current, total ->
                        notifier.showProgress(current, total)
                    }
                    notifier.showComplete()
                }
                onComplete(true)
            } catch (e: Exception) {
                notifier.showError()
                onComplete(false)
            }
        }
    }

    /**
     * Cancels ongoing work. Call from the BGProcessingTask expiration handler.
     */
    fun cancel() {
        scope.cancel()
        notifier.dismiss()
    }
}
