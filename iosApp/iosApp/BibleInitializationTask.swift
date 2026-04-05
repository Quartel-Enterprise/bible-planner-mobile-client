import BackgroundTasks
import ComposeApp

enum BibleInitializationTask {
    static let identifier = "com.quare.bibleplanner.bible_initialization"

    static func schedule() {
        let request = BGProcessingTaskRequest(identifier: identifier)
        request.requiresNetworkConnectivity = false
        request.requiresExternalPower = false
        try? BGTaskScheduler.shared.submit(request)
    }

    static func handle(_ task: BGProcessingTask) {
        let worker = IosBibleInitializationWorker()

        task.expirationHandler = {
            worker.cancel()
            task.setTaskCompleted(success: false)
        }

        worker.start { success in
            task.setTaskCompleted(success: success)
        }
    }
}
