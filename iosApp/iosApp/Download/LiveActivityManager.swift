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
        let wasPaused = pausedVersionIds.remove(versionId) != nil
        guard let activity = activities[versionId] else {
            // Still under the lock, so two progress updates can't each start an activity for the version.
            startLocked(versionId: versionId, versionName: versionName, progress: progress)
            lock.unlock()
            return
        }
        let last = lastReportedProgress[versionId] ?? -1
        let shouldUpdate = wasPaused || abs(progress - last) >= 0.01
        if shouldUpdate { lastReportedProgress[versionId] = progress }
        lock.unlock()

        guard shouldUpdate else { return }
        let state = BibleDownloadAttributes.ContentState(
            progress: progress,
            progressStr: formatProgress(progress),
            downloadState: .downloading,
            estimatedTimeLabel: estimatedTimeLabel
        )
        Task { await activity.update(.init(state: state, staleDate: nil)) }
    }

    /// The versions with a Live Activity this app tracks.
    var versionIds: [String] {
        lock.lock()
        defer { lock.unlock() }
        return Array(activities.keys)
    }

    /// Calls `completion` once the paused state reached the Live Activity, or right away without one.
    func pause(versionId: String, completion: @escaping () -> Void) {
        lock.lock()
        guard let activity = activities[versionId] else {
            dlog("pause SKIPPED — no active activity for \(versionId)", tag: "LIVE_ACTIVITY")
            lock.unlock()
            completion()
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
        Task {
            await activity.update(.init(state: state, staleDate: nil))
            completion()
        }
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

    /// Must be called with `lock` held.
    private func startLocked(versionId: String, versionName: String, progress: Double) {
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
            activities[versionId] = activity
            lastReportedProgress[versionId] = progress
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
