import ActivityKit
import Foundation

@available(iOS 16.2, *)
class LiveActivityManager {
    static let shared = LiveActivityManager()

    private var activities: [String: Activity<BibleDownloadAttributes>] = [:]
    private var lastReportedProgress: [String: Double] = [:]
    private let lock = NSLock()

    // MARK: - Lifecycle

    func start(versionId: String, versionName: String) {
        let enabled = ActivityAuthorizationInfo().areActivitiesEnabled
        dlog("start — versionId: \(versionId) versionName: \(versionName) areActivitiesEnabled: \(enabled)", tag: "LIVE_ACTIVITY")

        guard enabled else {
            dlog("start SKIPPED — activities not enabled on this device/iOS version", tag: "LIVE_ACTIVITY")
            return
        }

        lock.lock()
        if let existing = activities[versionId] {
            dlog("start — ending existing activity for \(versionId) before restart", tag: "LIVE_ACTIVITY")
            Task { await existing.end(nil, dismissalPolicy: .immediate) }
        }
        lock.unlock()

        let attributes = BibleDownloadAttributes(versionId: versionId, versionName: versionName)
        let initialState = BibleDownloadAttributes.ContentState(
            progress: 0,
            progressStr: "0",
            downloadState: .downloading,
            estimatedTimeLabel: nil
        )

        do {
            let activity = try Activity.request(
                attributes: attributes,
                contentState: initialState
            )
            lock.lock()
            activities[versionId] = activity
            lastReportedProgress[versionId] = 0
            lock.unlock()
            dlog("start SUCCESS — activityId: \(activity.id) state: \(activity.activityState)", tag: "LIVE_ACTIVITY")
            logAllActivities()
        } catch {
            dlog("start FAILED — error: \(error)", tag: "LIVE_ACTIVITY")
        }
    }

    func updateIfChanged(
        versionId: String,
        progress: Double,
        progressStr: String,
        estimatedTimeLabel: String?
    ) {
        lock.lock()
        let last = lastReportedProgress[versionId] ?? -1
        guard progress - last >= 0.01,
              let activity = activities[versionId] else {
            lock.unlock()
            return
        }
        lastReportedProgress[versionId] = progress
        lock.unlock()

        // Log state every 25% to avoid spam
        let pct = Int(progress * 100)
        if pct % 25 == 0 {
            dlog("update — \(versionId) \(progressStr)% state: \(activity.activityState)", tag: "LIVE_ACTIVITY")
        }
        let newState = BibleDownloadAttributes.ContentState(
            progress: progress,
            progressStr: progressStr,
            downloadState: .downloading,
            estimatedTimeLabel: estimatedTimeLabel
        )
        Task { await activity.update(.init(state: newState, staleDate: nil)) }
    }

    func pause(versionId: String) {
        lock.lock()
        guard let activity = activities[versionId],
              let progress = lastReportedProgress[versionId] else {
            dlog("pause SKIPPED — no active activity for \(versionId)", tag: "LIVE_ACTIVITY")
            lock.unlock()
            return
        }
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

    func resume(versionId: String) {
        lock.lock()
        guard let activity = activities[versionId],
              let progress = lastReportedProgress[versionId] else {
            dlog("resume SKIPPED — no active activity for \(versionId)", tag: "LIVE_ACTIVITY")
            lock.unlock()
            return
        }
        lock.unlock()

        dlog("resume — \(versionId) at \(Int(progress * 100))%", tag: "LIVE_ACTIVITY")
        let state = BibleDownloadAttributes.ContentState(
            progress: progress,
            progressStr: formatProgress(progress),
            downloadState: .downloading,
            estimatedTimeLabel: nil
        )
        Task { await activity.update(.init(state: state, staleDate: nil)) }
    }

    func end(versionId: String) {
        lock.lock()
        guard let activity = activities.removeValue(forKey: versionId) else {
            dlog("end SKIPPED — no active activity for \(versionId)", tag: "LIVE_ACTIVITY")
            lock.unlock()
            return
        }
        lastReportedProgress.removeValue(forKey: versionId)
        lock.unlock()

        dlog("end — \(versionId) currentState: \(activity.activityState) showing complete then dismissing in 5s", tag: "LIVE_ACTIVITY")
        let finalState = BibleDownloadAttributes.ContentState(
            progress: 1.0,
            progressStr: "100",
            downloadState: .complete,
            estimatedTimeLabel: nil
        )
        Task {
            await activity.end(
                using: finalState,
                dismissalPolicy: .after(.now + 5)
            )
            dlog("end — \(versionId) activity.end() completed", tag: "LIVE_ACTIVITY")
        }
    }

    // MARK: - Diagnostics

    func logAllActivities() {
        let all = Activity<BibleDownloadAttributes>.activities
        dlog("all active Live Activities count: \(all.count)", tag: "LIVE_ACTIVITY")
        for a in all {
            dlog("  → id:\(a.id) state:\(a.activityState) version:\(a.attributes.versionId)", tag: "LIVE_ACTIVITY")
        }
    }

    // MARK: - Helpers

    private func formatProgress(_ progress: Double) -> String {
        let rounded = Int((progress * 10000).rounded())
        let intPart = rounded / 100
        let fracPart = rounded % 100
        return fracPart == 0 ? "\(intPart)" : "\(intPart).\(String(format: "%02d", fracPart))"
    }
}
