import Foundation
import Shared

/// Mirrors the shared download engine's progress in the version's Live Activity, and routes the
/// Live Activity's pause, resume and cancel buttons back to it.
final class BibleDownloadLiveActivity: IosDownloadLiveActivity {
    private let lock = NSLock()

    // Where this run of the download started, to estimate the time left.
    private var runStarts: [String: (time: Date, progress: Double)] = [:]

    // Darwin notification observer pointers for the Live Activity buttons, per version.
    // Each entry holds the opaque pointers passed to CFNotificationCenterAddObserver.
    private var darwinTokens: [String: [UnsafeMutableRawPointer]] = [:]

    init() {
        LiveActivityManager.shared.adoptOrphanedActivities().forEach { registerActionObservers(versionId: $0) }
    }

    func showProgress(versionId: String, versionName: String, progress: Float) {
        let progress = Double(progress)
        // Every run opens at 0 before reading the stored progress; a resumed download must not drop back to 0.
        if progress == 0, (LiveActivityManager.shared.knownProgress(versionId: versionId) ?? 0) > 0 { return }
        lock.lock()
        let runStart = runStarts[versionId] ?? (time: Date(), progress: progress)
        runStarts[versionId] = runStart
        lock.unlock()

        registerActionObservers(versionId: versionId)
        LiveActivityManager.shared.show(
            versionId: versionId,
            versionName: versionName,
            progress: progress,
            estimatedTimeLabel: estimatedTimeLabel(progress: progress, runStart: runStart)
        )
    }

    func showPaused(versionId: String) {
        forgetRun(versionId: versionId)
        LiveActivityManager.shared.pause(versionId: versionId)
    }

    func showComplete(versionId: String) {
        forgetRun(versionId: versionId)
        unregisterActionObservers(versionId: versionId)
        LiveActivityManager.shared.complete(versionId: versionId)
    }

    func dismiss(versionId: String) {
        forgetRun(versionId: versionId)
        unregisterActionObservers(versionId: versionId)
        LiveActivityManager.shared.dismiss(versionId: versionId)
    }

    // MARK: - Darwin Notification Observers (Live Activity button IPC)

    private func registerActionObservers(versionId: String) {
        lock.lock()
        defer { lock.unlock() }
        guard darwinTokens[versionId] == nil else { return }
        darwinTokens[versionId] = ["pause", "resume", "cancel"].map { action in
            let name = "com.quare.bibleplanner.dl.\(action).\(versionId)" as CFString
            let wrapper = DarwinObserverWrapper {
                dlog("Darwin → handleDownloadAction(\(action), \(versionId))", tag: "LIVE_ACTIVITY")
                MainViewControllerKt.handleDownloadAction(action: action, versionId: versionId)
            }
            // passRetained keeps the wrapper alive until the observer is removed.
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
            return opaque
        }
    }

    private func unregisterActionObservers(versionId: String) {
        lock.lock()
        let opaques = darwinTokens.removeValue(forKey: versionId) ?? []
        lock.unlock()
        for opaque in opaques {
            CFNotificationCenterRemoveObserver(CFNotificationCenterGetDarwinNotifyCenter(), opaque, nil, nil)
            Unmanaged<DarwinObserverWrapper>.fromOpaque(opaque).release()
        }
    }

    // MARK: - Helpers

    private func forgetRun(versionId: String) {
        lock.lock()
        runStarts.removeValue(forKey: versionId)
        lock.unlock()
    }

    private func estimatedTimeLabel(progress: Double, runStart: (time: Date, progress: Double)) -> String? {
        let done = progress - runStart.progress
        guard done > 0.01 else { return nil }
        let rate = done / Date().timeIntervalSince(runStart.time)
        guard rate > 0 else { return nil }
        let remaining = (1.0 - progress) / rate
        return remaining < 60 ? "Est. \(Int(remaining))s left" : "Est. \(Int(remaining / 60))m left"
    }
}

/// Heap-allocated closure wrapper used as the observer context for
/// CFNotificationCenterAddObserver. Allows Swift closures to be used as C callbacks.
final class DarwinObserverWrapper {
    private let handler: () -> Void
    init(_ handler: @escaping () -> Void) { self.handler = handler }
    func fire() { handler() }
}
