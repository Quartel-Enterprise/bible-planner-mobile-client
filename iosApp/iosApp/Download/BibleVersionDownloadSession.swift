import Foundation
import ComposeApp

class BibleVersionDownloadSession: NSObject, URLSessionDownloadDelegate, IosDownloadSession {
    static let sessionIdentifier = "com.quare.bibleplanner.bible.download"
    static var shared: BibleVersionDownloadSession?

    private var bridge: IosBackgroundDownloadBridge?
    var backgroundCompletionHandler: (() -> Void)?

    private lazy var session: URLSession = {
        let config = URLSessionConfiguration.background(withIdentifier: Self.sessionIdentifier)
        config.sessionSendsLaunchEvents = true
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

        bridge?.processDownloadedChapter(
            chapterId: chapterId,
            versionId: versionId,
            jsonString: jsonString
        )
    }

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        guard let error = error as NSError?,
              error.code != NSURLErrorCancelled else { return }
        // Individual task errors are handled gracefully; the version stays IN_PROGRESS
        // and will resume on next launch or BGTask.
    }

    func urlSessionDidFinishEvents(forBackgroundURLSession session: URLSession) {
        DispatchQueue.main.async {
            self.backgroundCompletionHandler?()
            self.backgroundCompletionHandler = nil
        }
    }

    // MARK: - Helpers

    private func parseTaskDescription(_ description: String) -> (versionId: String, chapterId: Int64)? {
        guard let pipeIndex = description.firstIndex(of: "|") else { return nil }
        let versionId = String(description[..<pipeIndex])
        let chapterIdStr = String(description[description.index(after: pipeIndex)...])
        guard let chapterId = Int64(chapterIdStr) else { return nil }
        return (versionId, chapterId)
    }
}
