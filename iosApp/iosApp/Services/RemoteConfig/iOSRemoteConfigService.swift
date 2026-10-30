import Foundation
import FirebaseRemoteConfig
import Network
import Shared

class IosRemoteConfigService: RemoteConfigDataSource {
    private let remoteConfig: RemoteConfig
    private var initializationTask: Task<Void, Never>?
    private let networkTimeout: Duration = .seconds(5)

    init(remoteConfig: RemoteConfig) {
        self.remoteConfig = remoteConfig
        
        // Optimize settings for faster updates
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = 0
        remoteConfig.configSettings = settings
        
        initializationTask = Task {
            await waitForNetworkAndFetch()
        }
    }

    func getBoolean(key: String) async throws -> KotlinBoolean? {
        await initializationTask?.value
        let value = remoteConfig.configValue(forKey: key)
        guard value.source != .static else { return nil }
        return KotlinBoolean(bool: value.boolValue)
    }

    func getInt(key: String) async throws -> KotlinInt? {
        await initializationTask?.value
        let value = remoteConfig.configValue(forKey: key)
        guard value.source != .static else { return nil }
        return KotlinInt(int: value.numberValue.int32Value)
    }

    func getString(key: String) async throws -> String? {
        await initializationTask?.value
        let value = remoteConfig.configValue(forKey: key)
        guard value.source != .static else { return nil }
        return value.stringValue
    }

    func addConfigUpdateListener(onUpdate: @escaping () -> Void) -> Cancellable {
        let registration = remoteConfig.addOnConfigUpdateListener { [weak self] _, error in
            guard error == nil else { return }
            self?.remoteConfig.activate { _, _ in
                onUpdate()
            }
        }
        return ConfigUpdateCancellable(registration: registration)
    }

    private func waitForNetworkAndFetch() async {
        print("RemoteConfig: Monitoring network status...")

        /*
         * Why: offline, waiting for the network without a limit left every getter suspended
         * forever. Like Android, give up and serve the cached or default values; the config
         * update listener fetches once the connection comes back.
         */
        guard await waitForNetwork(timeout: networkTimeout) else {
            print("RemoteConfig: No network after \(networkTimeout), using cached or default values.")
            return
        }
        print("RemoteConfig: Network is satisfied.")

        await fetchAndActivateWithRetry()
    }

    /*
     * Why: iterating path updates instead of resuming a continuation from pathUpdateHandler,
     * which crashed by resuming twice when two .satisfied updates arrived back to back
     * (Crashlytics 689b6e8f). withTaskGroup waits for every child, so the network child must
     * end when the timeout cancels it: an AsyncStream iterator returns nil on cancellation.
     */
    private func waitForNetwork(timeout: Duration) async -> Bool {
        await withTaskGroup(of: Bool.self) { group in
            group.addTask {
                for await path in IosRemoteConfigService.networkPathUpdates() where path.status == .satisfied {
                    return true
                }
                return false
            }
            group.addTask {
                try? await Task.sleep(for: timeout)
                return false
            }
            let isSatisfied = await group.next() ?? false
            group.cancelAll()
            return isSatisfied
        }
    }

    private static func networkPathUpdates() -> AsyncStream<NWPath> {
        AsyncStream { continuation in
            let monitor = NWPathMonitor()
            monitor.pathUpdateHandler = { path in
                continuation.yield(path)
            }
            continuation.onTermination = { _ in
                monitor.cancel()
            }
            monitor.start(queue: DispatchQueue.global())
        }
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

    init(registration: ConfigUpdateListenerRegistration) {
        self.registration = registration
    }

    func cancel() {
        registration.remove()
    }
}
