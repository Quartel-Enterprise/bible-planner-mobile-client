import Foundation
import FirebaseRemoteConfig
import Network
import ComposeApp

class IosRemoteConfigService: RemoteConfigService {
    private let remoteConfig: RemoteConfig
    private var initializationTask: Task<Void, Never>?
    private let monitor = NWPathMonitor()

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

    func getBoolean(key: String) async throws -> KotlinBoolean {
        await initializationTask?.value
        let value = remoteConfig.configValue(forKey: key).boolValue
        return KotlinBoolean(bool: value)
    }

    func getInt(key: String) async throws -> KotlinInt {
        await initializationTask?.value
        let value = remoteConfig.configValue(forKey: key).numberValue.int32Value
        return KotlinInt(int: value)
    }

    func getString(key: String) async throws -> String {
        await initializationTask?.value
        return remoteConfig.configValue(forKey: key).stringValue ?? ""
    }

    private func waitForNetworkAndFetch() async {
        print("RemoteConfig: Monitoring network status...")
        
        // Wait for network to be satisfied (liberated by system/user)
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            monitor.pathUpdateHandler = { path in
                if path.status == .satisfied {
                    print("RemoteConfig: Network is satisfied.")
                    self.monitor.cancel()
                    continuation.resume()
                }
            }
            monitor.start(queue: DispatchQueue.global())
        }
        
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
