import Foundation
import Shared

/// Strings, plurals and formats in the in-app language.
///
/// The app language screen stores the chosen language in `AppleLanguages` while the app runs, but `Bundle.main`
/// keeps the language the app launched with, so the native screens read their tables from that language's bundle.
struct NativeLocalization: Equatable {
    let locale: Locale
    private let bundle: Bundle

    private static var bundlesByLanguage: [String: Bundle] = [:]

    static func current() -> NativeLocalization {
        let languageTag = UserDefaults.standard.stringArray(forKey: "AppleLanguages")?.first ?? "en"
        return NativeLocalization(locale: Locale(identifier: languageTag), bundle: resolveBundle(for: languageTag))
    }

    func text(_ key: String, table: String) -> String {
        bundle.localizedString(forKey: key, value: nil, table: table)
    }

    /// The passages of a day, such as "Genesis 1-3, Psalms 1".
    func readingLabel(_ passages: [PassageModel]) -> String {
        passages.map { passage in
            let name = bookName(passage.bookId)
            guard let chapterRanges = passage.chapterRanges, !chapterRanges.isEmpty else { return name }
            return "\(name) \(chapterRanges)"
        }
        .joined(separator: ", ")
    }

    func bookName(_ bookId: BookId) -> String {
        text("book_" + bookId.name.lowercased(), table: "BookNames")
    }

    private static func resolveBundle(for languageTag: String) -> Bundle {
        if let cached = bundlesByLanguage[languageTag] { return cached }
        // "pt-BR" has its own folder, while a regional tag such as "es-MX" falls back to its language.
        let languageCode = String(languageTag.prefix { $0 != "-" && $0 != "_" })
        var resolved = Bundle.main
        for candidate in [languageTag, languageCode] {
            if let path = Bundle.main.path(forResource: candidate, ofType: "lproj"), let bundle = Bundle(path: path) {
                resolved = bundle
                break
            }
        }
        bundlesByLanguage[languageTag] = resolved
        return resolved
    }
}

/// A Kotlin `LocalDate` read through its ISO-8601 `toString()`, which Swift sees as `description`.
struct PlanDate: Equatable {
    let date: Date

    init?(kotlinIsoDate: String) {
        let parts = kotlinIsoDate.split(separator: "-").compactMap { Int($0) }
        guard parts.count == 3 else { return nil }
        var components = DateComponents()
        components.year = parts[0]
        components.month = parts[1]
        components.day = parts[2]
        // Noon keeps the day the same in every time zone the calendar might apply.
        components.hour = 12
        guard let date = Calendar(identifier: .gregorian).date(from: components) else { return nil }
        self.date = date
    }

    func dayText(locale: Locale) -> String {
        date.formatted(.dateTime.day().locale(locale))
    }

    func monthText(locale: Locale) -> String {
        date.formatted(.dateTime.month(.abbreviated).locale(locale))
    }

    func yearText(locale: Locale) -> String {
        date.formatted(.dateTime.year().locale(locale))
    }

    func shortText(locale: Locale) -> String {
        date.formatted(.dateTime.day().month(.abbreviated).locale(locale))
    }
}
