import Foundation
import FirebaseRemoteConfig
import ComposeApp

class IosRemoteConfigService: RemoteConfigService {
    private let remoteConfig: RemoteConfig

    init(remoteConfig: RemoteConfig) {
        self.remoteConfig = remoteConfig
        let settings = RemoteConfigSettings()
        #if DEBUG
        settings.minimumFetchInterval = 0
        #endif
        self.remoteConfig.configSettings = settings
        
        Task {
            await fetchAndActivate()
        }
    }

    func getBoolean(key: String) async throws -> KotlinBoolean {
        let value = remoteConfig.configValue(forKey: key).boolValue
        return KotlinBoolean(bool: value)
    }

    func getInt(key: String) async throws -> KotlinInt {
        let value = remoteConfig.configValue(forKey: key).numberValue.int32Value
        return KotlinInt(int: value)
    }

    private func fetchAndActivate() async {
        do {
            let status = try await remoteConfig.fetchAndActivate()
            print("Firebase Remote Config fetch status: \(status)")
        } catch {
            print("Error fetching remote config: \(error)")
        }
    }
}
