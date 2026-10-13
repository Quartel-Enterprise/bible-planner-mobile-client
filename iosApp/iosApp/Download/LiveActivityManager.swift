import ActivityKit
import Foundation

class LiveActivityManager {
    static let shared = LiveActivityManager()

    private var activities: [String: Activity<BibleDownloadAttributes>] = [:]
    private var lastReportedProgress: [String: Double] = [:]
    private var pausedVersionIds = Set<String>()
    private let lock = NSLock()

    // MARK: - Lifecycle

    /// Starts the version's Live Activity on its first progress, then updates it. A paused one
    /// always switches back to downloading; otherwise changes below 1% are skipped.
    func show(versionId: String, versionName: String, progress: Double, estimatedTimeLabel: String?) {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else { return }
        lock.lock()
        if let stored = activities[versionId], !stored.isLive {
            activities.removeValue(forKey: versionId)
            lastReportedProgress.removeValue(forKey: versionId)
            pausedVersionIds.remove(versionId)
        }
        let activity = activities[versionId]
        let wasPaused = pausedVersionIds.remove(versionId) != nil
        let last = lastReportedProgress[versionId] ?? -1
        let shouldUpdate = activity != nil && (wasPaused || abs(progress - last) >= 0.01)
        if shouldUpdate { lastReportedProgress[versionId] = progress }
        lock.unlock()

        guard let activity else {
            start(versionId: versionId, versionName: versionName, progress: progress)
            return
        }
        guard shouldUpdate else { return }
        let state = BibleDownloadAttributes.ContentState(
            progress: progress,
            progressStr: formatProgress(progress),
            downloadState: .downloading,
            estimatedTimeLabel: estimatedTimeLabel
        )
        Task { await activity.update(.init(state: state, staleDate: nil)) }
    }

    /// The last progress shown for the version, if its Live Activity is still up.
    func knownProgress(versionId: String) -> Double? {
        lock.lock()
        defer { lock.unlock() }
        return activities[versionId]?.isLive == true ? lastReportedProgress[versionId] : nil
    }

    func pause(versionId: String) {
        lock.lock()
        guard let activity = activities[versionId] else {
            dlog("pause SKIPPED — no active activity for \(versionId)", tag: "LIVE_ACTIVITY")
            lock.unlock()
            return
        }
        pausedVersionIds.insert(versionId)
        let progress = lastReportedProgress[versionId] ?? 0
        lock.unlock()

        dlog("pause — \(versionId) at \(Int(progress * 100))%", tag: "LIVE_ACTIVITY")
        let state = BibleDownloadAttributes.ContentState(
            progress: progress,
            progressStr: formatProgress(progress),
            downloadState: .paused,
            estimatedTimeLabel: nil
        )
        Task { await activity.update(.init(state: state, staleDate: nil)) }
    }

    /// Shows the version as complete, then dismisses it after a few seconds.
    func complete(versionId: String) {
        guard let activity = remove(versionId: versionId) else { return }
        dlog("complete — \(versionId), dismissing in 5s", tag: "LIVE_ACTIVITY")
        let finalState = BibleDownloadAttributes.ContentState(
            progress: 1.0,
            progressStr: "100",
            downloadState: .complete,
            estimatedTimeLabel: nil
        )
        Task { await activity.end(.init(state: finalState, staleDate: nil), dismissalPolicy: .after(.now + 5)) }
    }

    func dismiss(versionId: String) {
        guard let activity = remove(versionId: versionId) else { return }
        dlog("dismiss — \(versionId)", tag: "LIVE_ACTIVITY")
        Task { await activity.end(nil, dismissalPolicy: .immediate) }
    }

    /// Re-adopts the Live Activities still up from before the app was last terminated, so they can be
    /// updated, paused or ended. Returns their version ids.
    func adoptOrphanedActivities() -> [String] {
        let all = Activity<BibleDownloadAttributes>.activities
        dlog("adoptOrphanedActivities — found \(all.count) system activities", tag: "LIVE_ACTIVITY")
        lock.lock()
        defer { lock.unlock() }
        return all.compactMap { activity in
            let versionId = activity.attributes.versionId
            guard activity.isLive, activities[versionId] == nil else { return nil }
            activities[versionId] = activity
            lastReportedProgress[versionId] = activity.content.state.progress
            if activity.content.state.downloadState == .paused {
                pausedVersionIds.insert(versionId)
            }
            return versionId
        }
    }

    // MARK: - Helpers

    private func start(versionId: String, versionName: String, progress: Double) {
        let attributes = BibleDownloadAttributes(versionId: versionId, versionName: versionName)
        let initialState = BibleDownloadAttributes.ContentState(
            progress: progress,
            progressStr: formatProgress(progress),
            downloadState: .downloading,
            estimatedTimeLabel: nil
        )
        do {
            let activity = try Activity.request(
                attributes: attributes,
                content: .init(state: initialState, staleDate: nil)
            )
            lock.lock()
            activities[versionId] = activity
            lastReportedProgress[versionId] = progress
            lock.unlock()
            dlog("start — \(versionId) activityId: \(activity.id)", tag: "LIVE_ACTIVITY")
        } catch {
            dlog("start FAILED — \(error)", tag: "LIVE_ACTIVITY")
        }
    }

    private func remove(versionId: String) -> Activity<BibleDownloadAttributes>? {
        lock.lock()
        defer { lock.unlock() }
        lastReportedProgress.removeValue(forKey: versionId)
        pausedVersionIds.remove(versionId)
        return activities.removeValue(forKey: versionId)
    }

    private func formatProgress(_ progress: Double) -> String {
        let rounded = Int((progress * 10000).rounded())
        let intPart = rounded / 100
        let fracPart = rounded % 100
        return fracPart == 0 ? "\(intPart)" : "\(intPart).\(String(format: "%02d", fracPart))"
    }
}

private extension Activity where Attributes == BibleDownloadAttributes {
    /// Ended or dismissed activities stay listed for a while but ignore updates.
    var isLive: Bool { activityState == .active || activityState == .stale }
}
