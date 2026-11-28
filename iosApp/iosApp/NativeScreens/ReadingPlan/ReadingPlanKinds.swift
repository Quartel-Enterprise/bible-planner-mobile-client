import Shared

/*
 * Kotlin enums reach Swift as classes, so these Swift enums mirror them by `name` to get exhaustive switches.
 * A name Kotlin adds later falls back to a neutral case until it gets its own text and symbol here.
 */

enum PlanModeKind: String {
    case new = "New"
    case onTrack = "OnTrack"
    case caughtUp = "CaughtUp"
    case ahead = "Ahead"
    case behind = "Behind"
    case done = "Done"

    init(_ mode: PlanMode) {
        self = PlanModeKind(rawValue: mode.name) ?? .onTrack
    }
}

enum WeekGroupKind: String {
    case current = "Current"
    case upcoming = "Upcoming"
    case completed = "Completed"

    init(_ group: WeekGroup) {
        self = WeekGroupKind(rawValue: group.name) ?? .upcoming
    }
}

enum ReadingPlanTypeKind: String {
    case chronological = "CHRONOLOGICAL"
    case books = "BOOKS"

    init(_ type: ReadingPlanType) {
        self = ReadingPlanTypeKind(rawValue: type.name) ?? .chronological
    }
}

enum OverflowOptionKind: String {
    case editStartDay = "EDIT_START_DAY"
    case deleteProgress = "DELETE_PROGRESS"

    init?(_ option: OverflowOption) {
        self.init(rawValue: option.name)
    }
}
