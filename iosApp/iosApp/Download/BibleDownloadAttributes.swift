import ActivityKit
import Foundation

// MARK: - Attributes
// This file must be added to BOTH the main app target AND the BibleDownloadWidget
// extension target via Xcode's Target Membership inspector.

@available(iOS 16.2, *)
struct BibleDownloadAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        /// Download progress from 0.0 to 1.0
        var progress: Double
        /// Formatted string, e.g. "67" or "67.25"
        var progressStr: String
        var downloadState: BibleDownloadState
        /// Optional label shown on lock screen, e.g. "Est. 45s left"
        var estimatedTimeLabel: String?
    }

    /// Stable identifier for the Bible version (e.g. "nvi")
    let versionId: String
    /// Display name shown in the Live Activity (e.g. "NVI")
    let versionName: String
}

@available(iOS 16.2, *)
enum BibleDownloadState: String, Codable, Hashable {
    case downloading
    case paused
    case complete
}
