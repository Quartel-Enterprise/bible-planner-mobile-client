import Foundation
import Shared

class BibleVersionDownloadSession: NSObject, URLSessionDownloadDelegate, IosDownloadSession {
    static let sessionIdentifier = "com.quare.bibleplanner.bible.download"
    static var shared: BibleVersionDownloadSession?

    // Bound the per-task retry count so a permanently broken URL (e.g. 404)
    // can't keep us in an infinite retry loop.
    private static let maxRetries = 2

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

    // Guards against double-finalization (per-version path + urlSessionDidFinishEvents path).
    private var finalizedVersionIds = Set<String>()

    // Set when Kotlin has finished enqueuing all addDownloadTask calls for a version.
    // Value is the authoritative total task count from Kotlin.
    private var expectedTaskCounts: [String: Int] = [:]

    // Darwin notification observer pointers for Live Activity button actions.
    // Registered per-version when a Live Activity starts; removed when it ends.
    // Each entry holds the opaque pointers passed to CFNotificationCenterAddObserver.
    private var darwinTokens: [String: [UnsafeMutableRawPointer]] = [:]

    // Per-version progress tracking for Live Activity updates
    private var versionTaskCounts: [String: (total: Int, completed: Int)] = [:]
    private var versionStartTimes: [String: Date] = [:]

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
        if #available(iOS 16.2, *) {
            LiveActivityManager.shared.adoptOrphanedActivities()
        }
    }

    // MARK: - IosDownloadSession

    func setBridge(bridge: IosBackgroundDownloadBridge) {
        self.bridge = bridge
    }

    func addDownloadTask(url: String, versionId: String, chapterId: Int64) {
        lock.lock()
        let current = versionTaskCounts[versionId] ?? (total: 0, completed: 0)
        versionTaskCounts[versionId] = (total: current.total + 1, completed: current.completed)
        lock.unlock()

        guard let downloadUrl = URL(string: url) else { return }
        let task = session.downloadTask(with: downloadUrl)
        task.taskDescription = "\(versionId)|\(chapterId)"
        task.resume()
    }

    func cancelDownloads(versionId: String) {
        unregisterActionObservers(versionId: versionId)
        lock.lock()
        versionTaskCounts.removeValue(forKey: versionId)
        versionStartTimes.removeValue(forKey: versionId)
        finalizedVersionIds.remove(versionId)
        expectedTaskCounts.removeValue(forKey: versionId)
        lock.unlock()

        session.getAllTasks { tasks in
            tasks
                .filter { $0.taskDescription?.hasPrefix("\(versionId)|") == true }
                .forEach { $0.cancel() }
        }
    }

    func cancelAllDownloads() {
        lock.lock()
        versionTaskCounts.removeAll()
        versionStartTimes.removeAll()
        lock.unlock()

        session.getAllTasks { tasks in
            tasks.forEach { $0.cancel() }
        }
    }

    func startLiveActivity(versionId: String, versionName: String) {
        lock.lock()
        versionTaskCounts[versionId] = (total: 0, completed: 0)
        versionStartTimes[versionId] = Date()
        lock.unlock()

        registerActionObservers(versionId: versionId)
        dlog("startLiveActivity — \(versionId) (\(versionName))", tag: "SESSION")
        if #available(iOS 16.2, *) {
            LiveActivityManager.shared.start(versionId: versionId, versionName: versionName)
        } else {
            dlog("startLiveActivity SKIPPED — iOS < 16.2", tag: "SESSION")
        }
    }

    func pauseLiveActivity(versionId: String) {
        dlog("pauseLiveActivity — \(versionId)", tag: "SESSION")
        if #available(iOS 16.2, *) {
            LiveActivityManager.shared.pause(versionId: versionId)
        }
    }

    func resumeLiveActivity(versionId: String) {
        registerActionObservers(versionId: versionId)
        dlog("resumeLiveActivity — \(versionId)", tag: "SESSION")
        if #available(iOS 16.2, *) {
            LiveActivityManager.shared.resume(versionId: versionId)
        }
    }

    func notifyAllTasksRegistered(versionId: String, totalTaskCount: Int32) {
        lock.lock()
        let total = Int(totalTaskCount)
        expectedTaskCounts[versionId] = total
        let completed = versionTaskCounts[versionId]?.completed ?? 0
        let alreadyDone = total > 0
            && completed >= total
            && !finalizedVersionIds.contains(versionId)
        if alreadyDone { finalizedVersionIds.insert(versionId) }
        lock.unlock()

        dlog("notifyAllTasksRegistered — \(versionId) expected:\(total) completed:\(completed)", tag: "SESSION")

        // All tasks may have already completed before this notification arrived.
        if alreadyDone {
            dlog("finalizeVersionIfComplete — tasks completed before registration, finalizing \(versionId)", tag: "SESSION")
            bridge?.finalizeVersionIfComplete(versionId: versionId) { [weak self] in
                dlog("finalizeVersionIfComplete — callback received for \(versionId)", tag: "SESSION")
                self?.endLiveActivity(versionId: versionId)
            }
        }
    }

    // MARK: - Darwin Notification Observers (Live Activity button IPC)

    /// Registers Darwin notification observers for pause/resume/cancel actions
    /// for the given version. Called when a Live Activity starts or resumes.
    private func registerActionObservers(versionId: String) {
        var opaques: [UnsafeMutableRawPointer] = []
        for action in ["pause", "resume", "cancel"] {
            let name = "com.quare.bibleplanner.dl.\(action).\(versionId)" as CFString
            let capturedAction = action
            let capturedVersionId = versionId
            let wrapper = DarwinObserverWrapper {
                dlog("Darwin → handleDownloadAction(\(capturedAction), \(capturedVersionId))", tag: "SESSION")
                MainViewControllerKt.handleDownloadAction(action: capturedAction, versionId: capturedVersionId)
            }
            // passRetained keeps the wrapper alive; we store the pointer so we can
            // release it and remove the observer later.
            let opaque = Unmanaged.passRetained(wrapper).toOpaque()
            CFNotificationCenterAddObserver(
                CFNotificationCenterGetDarwinNotifyCenter(),
                opaque,
                { _, observer, _, _, _ in
                    guard let observer else { return }
                    Unmanaged<DarwinObserverWrapper>.fromOpaque(observer).takeUnretainedValue().fire()
                },
                name,
                nil,
                .deliverImmediately
            )
            opaques.append(opaque)
        }
        lock.lock()
        darwinTokens[versionId] = opaques
        lock.unlock()
    }

    private func unregisterActionObservers(versionId: String) {
        lock.lock()
        let opaques = darwinTokens.removeValue(forKey: versionId) ?? []
        lock.unlock()
        for opaque in opaques {
            CFNotificationCenterRemoveObserver(
                CFNotificationCenterGetDarwinNotifyCenter(),
                opaque,
                nil,
                nil
            )
            Unmanaged<DarwinObserverWrapper>.fromOpaque(opaque).release()
        }
    }

    func endLiveActivity(versionId: String) {
        unregisterActionObservers(versionId: versionId)
        lock.lock()
        versionTaskCounts.removeValue(forKey: versionId)
        versionStartTimes.removeValue(forKey: versionId)
        lock.unlock()

        dlog("endLiveActivity — \(versionId)", tag: "SESSION")
        if #available(iOS 16.2, *) {
            LiveActivityManager.shared.end(versionId: versionId)
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
            let (versionId, chapterId, _) = parseTaskDescription(description),
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
        ) { [weak self] dbProgress in
            guard let self else { return }
            self.lock.lock()
            var counts = self.versionTaskCounts[versionId] ?? (total: 1, completed: 0)
            counts.completed += 1
            self.versionTaskCounts[versionId] = counts
            let knownTotal = self.expectedTaskCounts[versionId]
            let expectedTotal = knownTotal ?? 0
            let startTime = self.versionStartTimes[versionId] ?? Date()
            self.pendingProcessingCount -= 1
            let shouldFlush = self.pendingProcessingCount == 0 && self.sessionEventsFinished
            let versionDone = knownTotal != nil && expectedTotal > 0
                && counts.completed >= expectedTotal
                && !self.finalizedVersionIds.contains(versionId)
            if versionDone { self.finalizedVersionIds.insert(versionId) }
            self.lock.unlock()

            // Use DB-derived progress so the Live Activity matches the in-app display.
            // Task-count progress was relative to the current session (chapters missing
            // at download start), causing the notification to lag far behind the app UI
            // when resuming a previously paused download.
            let progress = Double(dbProgress)
            dlog("PROGRESS db-based for \(versionId): \(counts.completed)/\(expectedTotal) tasks, db=\(String(format: "%.4f", progress))", tag: "PROGRESS")

            if #available(iOS 16.2, *) {
                let progressStr = self.formatProgress(progress)
                let etaLabel = self.estimatedTimeLabel(progress: progress, startTime: startTime)
                LiveActivityManager.shared.updateIfChanged(
                    versionId: versionId,
                    progress: progress,
                    progressStr: progressStr,
                    estimatedTimeLabel: etaLabel
                )
            }

            if versionDone {
                dlog("finalizeVersionIfComplete — all tasks done for \(versionId), finalizing now", tag: "SESSION")
                self.bridge?.finalizeVersionIfComplete(versionId: versionId) { [weak self] in
                    dlog("finalizeVersionIfComplete — callback received for \(versionId)", tag: "SESSION")
                    self?.endLiveActivity(versionId: versionId)
                }
            }
            if shouldFlush { self.finalizeAndFlush() }
        }
    }

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        guard let error = error as NSError?,
              error.code != NSURLErrorCancelled,
              let description = task.taskDescription,
              let parsed = parseTaskDescription(description) else { return }

        let (versionId, chapterId, retryCount) = parsed

        if retryCount < Self.maxRetries, let url = task.originalRequest?.url {
            dlog("retrying chapter \(chapterId) of \(versionId) (attempt \(retryCount + 1)/\(Self.maxRetries)) — error code: \(error.code)", tag: "SESSION")
            let retry = self.session.downloadTask(with: url)
            retry.taskDescription = "\(versionId)|\(chapterId)|\(retryCount + 1)"
            retry.resume()
            return
        }

        dlog("permanent failure for chapter \(chapterId) of \(versionId) after \(retryCount) retries — error code: \(error.code)", tag: "SESSION")
        handlePermanentTaskFailure(versionId: versionId)
    }

    /// Counts a failed task toward the version's completion so the Live Activity
    /// can finalize instead of hanging at <100%. The Kotlin side decides whether
    /// the version is actually DONE based on DB state, not this counter.
    private func handlePermanentTaskFailure(versionId: String) {
        lock.lock()
        var counts = versionTaskCounts[versionId] ?? (total: 1, completed: 0)
        counts.completed += 1
        versionTaskCounts[versionId] = counts
        let knownTotal = expectedTaskCounts[versionId]
        let expectedTotal = knownTotal ?? 0
        let progress = knownTotal != nil && expectedTotal > 0
            ? min(1.0, Double(counts.completed) / Double(expectedTotal))
            : 0
        let startTime = versionStartTimes[versionId] ?? Date()
        let versionDone = knownTotal != nil && expectedTotal > 0
            && counts.completed >= expectedTotal
            && !finalizedVersionIds.contains(versionId)
        if versionDone { finalizedVersionIds.insert(versionId) }
        lock.unlock()

        if #available(iOS 16.2, *) {
            let progressStr = formatProgress(progress)
            let etaLabel = estimatedTimeLabel(progress: progress, startTime: startTime)
            LiveActivityManager.shared.updateIfChanged(
                versionId: versionId,
                progress: progress,
                progressStr: progressStr,
                estimatedTimeLabel: etaLabel
            )
        }

        if versionDone {
            dlog("finalizeVersionIfComplete — last task failed permanently, finalizing \(versionId)", tag: "SESSION")
            bridge?.finalizeVersionIfComplete(versionId: versionId) { [weak self] in
                self?.endLiveActivity(versionId: versionId)
            }
        }
    }

    func urlSessionDidFinishEvents(forBackgroundURLSession session: URLSession) {
        lock.lock()
        sessionEventsFinished = true
        let shouldFlush = pendingProcessingCount == 0
        lock.unlock()
        dlog("urlSessionDidFinishEvents — pendingProcessingCount: \(pendingProcessingCount) shouldFlush: \(shouldFlush)", tag: "SESSION")
        if shouldFlush { finalizeAndFlush() }
    }

    // MARK: - Finalization

    /// Runs one finalization check per unique version (instead of per chapter),
    /// then calls the system backgroundCompletionHandler once all are done.
    private func finalizeAndFlush() {
        lock.lock()
        let versionIds = processedVersionIds.subtracting(finalizedVersionIds)
        processedVersionIds.removeAll()
        finalizedVersionIds.removeAll()
        lock.unlock()

        dlog("finalizeAndFlush — versions not yet finalized: \(versionIds)", tag: "SESSION")

        guard !versionIds.isEmpty else {
            dlog("finalizeAndFlush — all versions already finalized, calling backgroundCompletionHandler", tag: "SESSION")
            callBackgroundCompletionHandler()
            return
        }

        let group = DispatchGroup()
        for versionId in versionIds {
            group.enter()
            dlog("finalizeVersionIfComplete — calling for \(versionId)", tag: "SESSION")
            bridge?.finalizeVersionIfComplete(versionId: versionId) { [weak self] in
                dlog("finalizeVersionIfComplete — callback received for \(versionId)", tag: "SESSION")
                self?.endLiveActivity(versionId: versionId)
                group.leave()
            }
        }
        group.notify(queue: .main) { [weak self] in
            dlog("finalizeAndFlush — all versions finalized, calling backgroundCompletionHandler", tag: "SESSION")
            self?.callBackgroundCompletionHandler()
        }
    }

    private func callBackgroundCompletionHandler() {
        DispatchQueue.main.async {
            self.backgroundCompletionHandler?()
            self.backgroundCompletionHandler = nil
            self.lock.lock()
            self.sessionEventsFinished = false
            self.expectedTaskCounts.removeAll()
            self.finalizedVersionIds.removeAll()
            self.lock.unlock()
        }
    }

    // MARK: - Helpers

    private func parseTaskDescription(_ description: String) -> (versionId: String, chapterId: Int64, retryCount: Int)? {
        let parts = description.split(separator: "|", maxSplits: 2, omittingEmptySubsequences: false)
        guard parts.count >= 2 else { return nil }
        let versionId = String(parts[0])
        guard let chapterId = Int64(parts[1]) else { return nil }
        let retryCount = parts.count > 2 ? (Int(parts[2]) ?? 0) : 0
        return (versionId, chapterId, retryCount)
    }

    private func formatProgress(_ progress: Double) -> String {
        let rounded = Int((progress * 10000).rounded())
        let intPart = rounded / 100
        let fracPart = rounded % 100
        return fracPart == 0 ? "\(intPart)" : "\(intPart).\(String(format: "%02d", fracPart))"
    }

    private func estimatedTimeLabel(progress: Double, startTime: Date) -> String? {
        guard progress > 0.01 else { return nil }
        let elapsed = Date().timeIntervalSince(startTime)
        let rate = progress / elapsed
        guard rate > 0 else { return nil }
        let remaining = (1.0 - progress) / rate
        if remaining < 60 {
            return "Est. \(Int(remaining))s left"
        } else {
            return "Est. \(Int(remaining / 60))m left"
        }
    }
}

// MARK: - Darwin observer wrapper

/// Heap-allocated closure wrapper used as the observer context for
/// CFNotificationCenterAddObserver. Allows Swift closures to be used as C callbacks.
final class DarwinObserverWrapper {
    private let handler: () -> Void
    init(_ handler: @escaping () -> Void) { self.handler = handler }
    func fire() { handler() }
}
