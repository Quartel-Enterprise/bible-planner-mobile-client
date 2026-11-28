import Foundation

/// Strings, plurals and formats in the in-app language.
///
/// The app language screen stores the chosen language in `AppleLanguages` while the app runs, but `Bundle.main`
/// keeps the language the app launched with, so the native screens read their tables from that language's bundle.
struct NativeLocalization: Equatable {
    let languageTag: String
    let locale: Locale
    private let bundle: Bundle

    private static var bundlesByLanguage: [String: Bundle] = [:]

    /// The first of `AppleLanguages`, which the app language screen sets.
    static func currentLanguageTag() -> String {
        UserDefaults.standard.stringArray(forKey: "AppleLanguages")?.first ?? "en"
    }

    static func current() -> NativeLocalization {
        let languageTag = currentLanguageTag()
        return NativeLocalization(
            languageTag: languageTag,
            locale: Locale(identifier: languageTag),
            bundle: resolveBundle(for: languageTag)
        )
    }

    func text(_ key: String, table: String) -> String {
        bundle.localizedString(forKey: key, value: nil, table: table)
    }

    private static func resolveBundle(for languageTag: String) -> Bundle {
        if let cached = bundlesByLanguage[languageTag] { return cached }
        let folder = appLanguageFolder(for: languageTag)
        let resolved = Bundle.main.path(forResource: folder, ofType: "lproj").flatMap(Bundle.init(path:)) ?? Bundle.main
        bundlesByLanguage[languageTag] = resolved
        return resolved
    }

    /// The same mapping as Kotlin's `IosLanguageProvider`, so a language the app doesn't have (such as "pt-PT" or
    /// "fr") shows English here as it does on every Compose screen, instead of whatever `Bundle.main` matches.
    private static func appLanguageFolder(for languageTag: String) -> String {
        let parts = languageTag.replacingOccurrences(of: "_", with: "-").split(separator: "-")
        let language = parts.first.map(String.init) ?? ""
        let region = parts.count > 1 ? String(parts[1]) : ""
        if language == "pt", region == "BR" { return "pt-BR" }
        if language.hasPrefix("es") { return "es" }
        return "en"
    }
}

/// A Kotlin `LocalDate` read through its ISO-8601 `toString()`, which Swift sees as `description`.
struct PlanDate: Equatable {
    /// Built once: every day row parses a date on each render.
    private static let calendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = .autoupdatingCurrent
        return calendar
    }()

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
        guard let date = Self.calendar.date(from: components) else { return nil }
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
