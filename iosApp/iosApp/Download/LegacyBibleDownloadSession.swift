import Foundation

/// The background URLSession that downloaded Bible versions before 2.14.0. Its tasks live in the
/// system and outlive an app update, so a download running during the update would keep fetching
/// chapters nobody reads.
enum LegacyBibleDownloadSession {
    private static let identifier = "com.quare.bibleplanner.bible.download"

    static func cancelLeftoverTasks() {
        let configuration = URLSessionConfiguration.background(withIdentifier: identifier)
        URLSession(configuration: configuration).invalidateAndCancel()
    }
}
