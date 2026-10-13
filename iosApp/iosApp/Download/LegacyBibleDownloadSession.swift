import Foundation

/// The background URLSession that downloaded Bible versions before 2.14.0. Its tasks live in the
/// system and outlive an app update, so a download running during the update would keep fetching
/// chapters nobody reads.
enum LegacyBibleDownloadSession {
    private static let identifier = "com.quare.bibleplanner.bible.download"
    private static let cancelledKey = "legacy_bible_download_session_cancelled"

    /// Only the first launch after the update can find leftover tasks, so later launches skip the session.
    static func cancelLeftoverTasks() {
        let defaults = UserDefaults.standard
        guard !defaults.bool(forKey: cancelledKey) else { return }
        let configuration = URLSessionConfiguration.background(withIdentifier: identifier)
        URLSession(configuration: configuration).invalidateAndCancel()
        defaults.set(true, forKey: cancelledKey)
    }
}
