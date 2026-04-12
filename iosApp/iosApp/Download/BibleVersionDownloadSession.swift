import Foundation
import ComposeApp

class BibleVersionDownloadSession: NSObject, URLSessionDownloadDelegate, IosDownloadSession {
    static let sessionIdentifier = "com.quare.bibleplanner.bible.download"
    static var shared: BibleVersionDownloadSession?

    private var bridge: IosBackgroundDownloadBridge?
    var backgroundCompletionHandler: (() -> Void)?

    // Tracks in-flight processDownloadedChapter coroutines so we don't tell iOS
    // "we're done" before the DB writes actually complete.
    private let lock = NSLock()
    private var pendingProcessingCount: Int = 0
    private var sessionEventsFinished: Bool = false
    // Collects the unique version IDs seen during this background session so we
    // can run a single finalization check per version instead of one per chapter.
    private var processedVersionIds = Set<String>()

    private lazy var session: URLSession = {
        let config = URLSessionConfiguration.background(withIdentifier: Self.sessionIdentifier)
        config.sessionSendsLaunchEvents = true
        config.isDiscretionary = false
        return URLSession(configuration: config, delegate: self, delegateQueue: nil)
    }()

    override init() {
        super.init()
        Self.shared = self
        _ = session // Force lazy init so iOS reconnects pending background tasks
    }

    // MARK: - IosDownloadSession

    func setBridge(bridge: IosBackgroundDownloadBridge) {
        self.bridge = bridge
    }

    func addDownloadTask(url: String, versionId: String, chapterId: Int64) {
        guard let downloadUrl = URL(string: url) else { return }
        let task = session.downloadTask(with: downloadUrl)
        task.taskDescription = "\(versionId)|\(chapterId)"
        task.resume()
    }

    func cancelDownloads(versionId: String) {
        session.getAllTasks { tasks in
            tasks
                .filter { $0.taskDescription?.hasPrefix("\(versionId)|") == true }
                .forEach { $0.cancel() }
        }
    }

    func cancelAllDownloads() {
        session.getAllTasks { tasks in
            tasks.forEach { $0.cancel() }
        }
    }

    // MARK: - URLSessionDownloadDelegate

    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didFinishDownloadingTo location: URL
    ) {
        guard
            let description = downloadTask.taskDescription,
            let (versionId, chapterId) = parseTaskDescription(description),
            let jsonString = try? String(contentsOf: location, encoding: .utf8)
        else { return }

        lock.lock()
        pendingProcessingCount += 1
        processedVersionIds.insert(versionId)
        lock.unlock()

        bridge?.processDownloadedChapter(
            chapterId: chapterId,
            versionId: versionId,
            jsonString: jsonString
        ) { [weak self] in
            guard let self else { return }
            self.lock.lock()
            self.pendingProcessingCount -= 1
            let shouldFlush = self.pendingProcessingCount == 0 && self.sessionEventsFinished
            self.lock.unlock()
            if shouldFlush { self.finalizeAndFlush() }
        }
    }

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        guard let error = error as NSError?,
              error.code != NSURLErrorCancelled else { return }
        // Individual task errors are handled gracefully; the version stays IN_PROGRESS
        // and will resume on the next launch.
    }

    func urlSessionDidFinishEvents(forBackgroundURLSession session: URLSession) {
        lock.lock()
        sessionEventsFinished = true
        let shouldFlush = pendingProcessingCount == 0
        lock.unlock()
        if shouldFlush { finalizeAndFlush() }
    }

    // MARK: - Helpers

    /// Runs one finalization check per unique version (instead of per chapter),
    /// then calls the system backgroundCompletionHandler once all are done.
    private func finalizeAndFlush() {
        lock.lock()
        let versionIds = processedVersionIds
        processedVersionIds.removeAll()
        lock.unlock()

        guard !versionIds.isEmpty else {
            callBackgroundCompletionHandler()
            return
        }

        let group = DispatchGroup()
        for versionId in versionIds {
            group.enter()
            bridge?.finalizeVersionIfComplete(versionId: versionId) {
                group.leave()
            }
        }
        group.notify(queue: .main) { [weak self] in
            self?.callBackgroundCompletionHandler()
        }
    }

    private func callBackgroundCompletionHandler() {
        DispatchQueue.main.async {
            self.backgroundCompletionHandler?()
            self.backgroundCompletionHandler = nil
            self.sessionEventsFinished = false
        }
    }

    private func parseTaskDescription(_ description: String) -> (versionId: String, chapterId: Int64)? {
        guard let pipeIndex = description.firstIndex(of: "|") else { return nil }
        let versionId = String(description[..<pipeIndex])
        let chapterIdStr = String(description[description.index(after: pipeIndex)...])
        guard let chapterId = Int64(chapterIdStr) else { return nil }
        return (versionId, chapterId)
    }
}
