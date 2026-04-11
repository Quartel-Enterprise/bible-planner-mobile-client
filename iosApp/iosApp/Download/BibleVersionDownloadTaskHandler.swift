import BackgroundTasks

enum BibleVersionDownloadTaskHandler {
    static let taskIdentifier = "com.quare.bibleplanner.bible_download"

    /// Registers the BGProcessingTask handler. Must be called during app launch,
    /// before the app finishes launching (i.e. in iOSApp.init()).
    static func register(scheduler: IosBackgroundTaskSchedulerImpl) {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: taskIdentifier,
            using: nil
        ) { [scheduler] task in
            guard let processingTask = task as? BGProcessingTask else { return }
            handle(task: processingTask, scheduler: scheduler)
        }
    }

    private static func handle(task: BGProcessingTask, scheduler: IosBackgroundTaskSchedulerImpl) {
        task.expirationHandler = {
            scheduler.expireDownloads {
                task.setTaskCompleted(success: false)
            }
        }

        scheduler.resumeDownloads { success in
            task.setTaskCompleted(success: success)
        }
    }
}
