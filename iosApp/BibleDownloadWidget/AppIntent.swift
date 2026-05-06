import AppIntents
import Foundation
import WidgetKit

// MARK: - Widget Configuration (keep for BibleDownloadWidget.swift)

struct ConfigurationAppIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource { "Configuration" }
    static var description: IntentDescription { "This is an example widget." }

    @Parameter(title: "Favorite Emoji", default: "😃")
    var favoriteEmoji: String
}

// MARK: - Live Activity Action Intents

struct PauseDownloadIntent: AppIntent {
    static var title: LocalizedStringResource = "Pause Download"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Version ID")
    var versionId: String

    func perform() async throws -> some IntentResult {
        postDarwinAction("pause", versionId: versionId)
        return .result()
    }
}

struct ResumeDownloadIntent: AppIntent {
    static var title: LocalizedStringResource = "Resume Download"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Version ID")
    var versionId: String

    func perform() async throws -> some IntentResult {
        postDarwinAction("resume", versionId: versionId)
        return .result()
    }
}

struct CancelDownloadIntent: AppIntent {
    static var title: LocalizedStringResource = "Cancel Download"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Version ID")
    var versionId: String

    func perform() async throws -> some IntentResult {
        postDarwinAction("cancel", versionId: versionId)
        return .result()
    }
}

private func postDarwinAction(_ action: String, versionId: String) {
    let name = "com.quare.bibleplanner.dl.\(action).\(versionId)" as CFString
    CFNotificationCenterPostNotification(
        CFNotificationCenterGetDarwinNotifyCenter(),
        CFNotificationName(name),
        nil, nil, true
    )
}
