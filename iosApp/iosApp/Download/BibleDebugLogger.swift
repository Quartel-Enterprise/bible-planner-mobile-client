import Foundation
import UIKit

/// Writes timestamped log entries to Documents/bible_planner_debug.log
/// and mirrors every entry to NSLog (visible in Xcode console + Console.app).
///
/// Retrieve the file:
///   Xcode → Window → Devices and Simulators → select device
///   → BiblePlanner → Download Container → right-click .xcappdata
///   → Show Package Contents → AppData/Documents/bible_planner_debug.log
final class BibleDebugLogger {
    static let shared = BibleDebugLogger()

    private let fileURL: URL
    private let queue = DispatchQueue(label: "com.quare.bibleplanner.debuglogger")
    private let timeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
        return f
    }()

    private init() {
        let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        fileURL = docs.appendingPathComponent("bible_planner_debug.log")
        // Reset the file on every app launch so each log is a clean session.
        try? "".data(using: .utf8)?.write(to: fileURL, options: .atomic)
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        log("App launched — log at: \(fileURL.path)")
        log("iOS \(UIDevice.current.systemVersion) | \(UIDevice.current.model)")
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    func log(
        _ message: String,
        tag: String = "APP",
        file: String = #file,
        function: String = #function,
        line: Int = #line
    ) {
        let ts = timeFormatter.string(from: Date())
        let src = "\(URL(fileURLWithPath: file).lastPathComponent):\(line)"
        let entry = "[\(ts)] [\(tag)] \(src) \(function) → \(message)\n"

        NSLog("[BibleDebug][\(tag)] %@:%d %@ → %@",
              URL(fileURLWithPath: file).lastPathComponent, line, function, message)

        queue.async { [weak self] in
            guard let self else { return }
            guard let data = entry.data(using: .utf8) else { return }
            if FileManager.default.fileExists(atPath: self.fileURL.path),
               let handle = try? FileHandle(forWritingTo: self.fileURL) {
                handle.seekToEndOfFile()
                handle.write(data)
                try? handle.close()
            } else {
                try? data.write(to: self.fileURL, options: .atomic)
            }
        }
    }
}

/// Convenience top-level function so call sites don't need the full qualifier.
func dlog(_ message: String, tag: String = "APP", file: String = #file, function: String = #function, line: Int = #line) {
    BibleDebugLogger.shared.log(message, tag: tag, file: file, function: function, line: line)
}
