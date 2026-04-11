import BackgroundTasks
import ComposeApp

class IosBackgroundTaskSchedulerImpl: IosBackgroundTaskScheduler {
    static let taskIdentifier = BibleVersionDownloadTaskHandler.taskIdentifier

    private var handler: (any BackgroundDownloadHandler)?

    func scheduleDownload() {
        let request = BGProcessingTaskRequest(identifier: Self.taskIdentifier)
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            // Task already scheduled or BGTaskScheduler not available in this environment
        }
    }

    func cancelDownload() {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: Self.taskIdentifier)
    }

    func setHandler(handler: (any BackgroundDownloadHandler)?) {
        self.handler = handler
    }

    func resumeDownloads(onComplete: @escaping (Bool) -> Void) {
        handler?.onResume { onComplete($0.boolValue) }
    }

    func expireDownloads(onComplete: @escaping () -> Void) {
        handler?.onExpire(onComplete: onComplete)
    }
}
