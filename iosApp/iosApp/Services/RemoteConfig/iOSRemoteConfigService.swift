import Foundation
import FirebaseRemoteConfig
import Network
import os
import Shared

class IosRemoteConfigService: RemoteConfigDataSource {
    private let remoteConfig: RemoteConfig
    private var initializationTask: Task<Void, Never>?
    private let isInitialized = OSAllocatedUnfairLock(initialState: false)
    private let updateListeners = OSAllocatedUnfairLock(uncheckedState: [UUID: () -> Void]())
    private let initializationTimeout: Duration = .seconds(5)

    init(remoteConfig: RemoteConfig) {
        self.remoteConfig = remoteConfig
        
        // Optimize settings for faster updates
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = 0
        remoteConfig.configSettings = settings
        
        initializationTask = Task {
            await waitForNetworkAndFetch()
            isInitialized.withLock { $0 = true }
            /*
             * Why: a getter that gave up waiting returned cached or default values, and Firebase's
             * config update listener only fires for server-side changes, not for this first fetch.
             */
            notifyUpdateListeners()
        }
    }

    func getBoolean(key: String) async throws -> KotlinBoolean? {
        await awaitInitialization()
        let value = remoteConfig.configValue(forKey: key)
        guard value.source != .static else { return nil }
        return KotlinBoolean(bool: value.boolValue)
    }

    func getInt(key: String) async throws -> KotlinInt? {
        await awaitInitialization()
        let value = remoteConfig.configValue(forKey: key)
        guard value.source != .static else { return nil }
        return KotlinInt(int: value.numberValue.int32Value)
    }

    func getString(key: String) async throws -> String? {
        await awaitInitialization()
        let value = remoteConfig.configValue(forKey: key)
        guard value.source != .static else { return nil }
        return value.stringValue
    }

    func addConfigUpdateListener(onUpdate: @escaping () -> Void) -> Cancellable {
        let id = UUID()
        updateListeners.withLockUnchecked { $0[id] = onUpdate }
        let registration = remoteConfig.addOnConfigUpdateListener { [weak self] _, error in
            guard error == nil else { return }
            self?.remoteConfig.activate { _, _ in
                onUpdate()
            }
        }
        return ConfigUpdateCancellable(registration: registration) { [updateListeners] in
            updateListeners.withLockUnchecked { $0[id] = nil }
        }
    }

    /*
     * Why: the first fetch waits for the network without a limit (the first launch can sit on the
     * "allow wireless data" prompt), and a fetch can hang on a captive portal, so getters stop
     * waiting after a while and serve cached or default values instead of suspending forever.
     * Awaiting the task's value ignores cancellation, hence the race through a single-shot
     * continuation instead of a task group.
     */
    private func awaitInitialization() async {
        guard let initializationTask, !isInitialized.withLock({ $0 }) else { return }
        let timeout = initializationTimeout
        let isResumed = OSAllocatedUnfairLock(initialState: false)
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            let resumeOnce: @Sendable () -> Void = {
                let isFirst = isResumed.withLock { resumed in
                    defer { resumed = true }
                    return !resumed
                }
                if isFirst {
                    continuation.resume()
                }
            }
            Task {
                await initializationTask.value
                resumeOnce()
            }
            Task {
                try? await Task.sleep(for: timeout)
                resumeOnce()
            }
        }
    }

    private func notifyUpdateListeners() {
        let listeners = updateListeners.withLockUnchecked { Array($0.values) }
        listeners.forEach { $0() }
    }

    private func waitForNetworkAndFetch() async {
        print("RemoteConfig: Monitoring network status...")

        /*
         * Why: iterating the monitor instead of resuming a continuation from pathUpdateHandler,
         * which crashed by resuming twice when two .satisfied updates arrived back to back
         * (Crashlytics 689b6e8f). Leaving the loop stops the monitor.
         */
        for await path in NWPathMonitor() where path.status == .satisfied {
            break
        }
        print("RemoteConfig: Network is satisfied.")

        await fetchAndActivateWithRetry()
    }

    private func fetchAndActivateWithRetry() async {
        var attempts = 0
        let maxAttempts = 3
        
        while attempts < maxAttempts {
            do {
                let status = try await remoteConfig.fetchAndActivate()
                print("Firebase Remote Config fetch status: \(status)")
                
                if status != .error {
                    // Success or already up to date
                    return
                }
                
                // If we got here, it's .error status but no exception was thrown
                attempts += 1
            } catch {
                attempts += 1
                print("Error fetching remote config (attempt \(attempts)): \(error.localizedDescription)")
            }
            
            if attempts < maxAttempts {
                // Wait before retrying
                let delay = UInt64(attempts * 2) * 1_000_000_000
                print("Retrying Remote Config fetch in \(attempts * 2) seconds...")
                try? await Task.sleep(nanoseconds: delay)
            }
        }
    }
}

private class ConfigUpdateCancellable: Cancellable {
    private let registration: ConfigUpdateListenerRegistration
    private let onCancel: () -> Void

    init(registration: ConfigUpdateListenerRegistration, onCancel: @escaping () -> Void) {
        self.registration = registration
        self.onCancel = onCancel
    }

    func cancel() {
        registration.remove()
        onCancel()
    }
}
