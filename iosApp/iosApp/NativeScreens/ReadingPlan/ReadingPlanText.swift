import Shared
import SwiftUI

/// Maps the reading plan's state to text and SF Symbols in the in-app language.
struct ReadingPlanText: Equatable {
    static let table = "ReadingPlan"

    let localization: NativeLocalization

    var locale: Locale { localization.locale }

    func text(_ key: String) -> String {
        localization.text(key, table: Self.table)
    }

    /// Formats a string or a `.stringsdict` plural; integers go in as `Int` for the `%ld` specifiers.
    func format(_ key: String, _ arguments: CVarArg...) -> String {
        String(format: text(key), locale: locale, arguments: arguments)
    }

    func readingLabel(_ passages: [PassageModel]) -> String {
        localization.readingLabel(passages)
    }

    // MARK: Plan order and menu

    func planTypeTitle(_ type: ReadingPlanType) -> String {
        switch ReadingPlanTypeKind(type) {
        case .chronological: text("order_chronological")
        case .books: text("order_books")
        }
    }

    func overflowTitle(_ option: OverflowOptionKind) -> String {
        switch option {
        case .editStartDay: text("edit_start_day")
        case .deleteProgress: text("delete_progress")
        }
    }

    func overflowSymbol(_ option: OverflowOptionKind) -> String {
        switch option {
        case .editStartDay: "calendar"
        case .deleteProgress: "trash"
        }
    }

    /// The toolbar shortcut back to the active day, the native take on the Compose floating button.
    func shortcutTitle(_ mode: PlanModeKind) -> String? {
        switch mode {
        case .ahead, .caughtUp: text("shortcut_next_reading")
        case .behind: text("shortcut_resume")
        case .new, .onTrack: text("shortcut_back_to_today")
        case .done: nil
        }
    }

    func shortcutSymbol(_ mode: PlanModeKind) -> String {
        switch mode {
        case .behind: "clock.arrow.circlepath"
        case .new, .onTrack: "calendar"
        case .ahead, .caughtUp, .done: "arrow.forward.circle"
        }
    }

    // MARK: Hero

    func heroKicker(_ mode: PlanModeKind) -> String {
        switch mode {
        case .new: text("hero_kicker_new")
        case .caughtUp: text("hero_kicker_caughtup")
        case .ahead: text("hero_kicker_ahead")
        case .behind: text("hero_kicker_behind")
        case .onTrack, .done: text("hero_kicker_today")
        }
    }

    func heroKickerSymbol(_ mode: PlanModeKind) -> String? {
        switch mode {
        case .caughtUp: "checkmark.circle.fill"
        case .ahead: "bolt.fill"
        case .behind: "clock.arrow.circlepath"
        case .new, .onTrack, .done: nil
        }
    }

    func heroPrimaryTitle(_ mode: PlanModeKind) -> String {
        switch mode {
        case .new: text("hero_primary_start")
        case .caughtUp: text("hero_primary_read_ahead")
        case .ahead: text("hero_primary_continue")
        case .behind: text("hero_primary_resume")
        case .onTrack, .done: text("hero_primary_read_now")
        }
    }

    func heroDayText(_ mode: PlanModeKind, dayIndex: Int32, totalDays: Int32) -> String {
        switch mode {
        case .ahead: format("hero_next_day", Int(dayIndex))
        case .caughtUp: format("hero_tomorrow_day", Int(dayIndex))
        case .new, .onTrack, .behind, .done: format("hero_day_of_total", Int(dayIndex), Int(totalDays))
        }
    }

    func heroNote(_ mode: PlanModeKind, daysAhead: Int32) -> String? {
        switch mode {
        case .new: text("hero_new_note")
        case .caughtUp: text("hero_caughtup_note")
        case .ahead: format("days_ahead_note", Int(daysAhead))
        case .onTrack, .behind, .done: nil
        }
    }

    // MARK: Weeks

    func sectionTitle(_ group: WeekGroupKind, isBehind: Bool) -> String {
        switch group {
        case .current: isBehind ? text("section_current_behind") : text("section_current")
        case .upcoming: text("section_upcoming")
        case .completed: text("section_completed")
        }
    }

    // MARK: Motivation

    func motivation(_ message: PlanMotivationMessage) -> String {
        if let milestone = message as? PlanMotivationMessageMilestoneOnlyOneBookLeft {
            return format("motivation_milestone_only_one_left", localization.bookName(milestone.bookId))
        }
        if let milestone = message as? PlanMotivationMessageMilestoneBookCompleted {
            return format("motivation_milestone_book_completed", localization.bookName(milestone.bookId))
        }
        return text(motivationKey(message))
    }

    private func motivationKey(_ message: PlanMotivationMessage) -> String {
        switch message {
        case is PlanMotivationMessageOverallProgressZero: "motivation_overall_zero"
        case is PlanMotivationMessageOverallProgressEarlyStart: "motivation_overall_early_start"
        case is PlanMotivationMessageOverallProgressBuildingSolid: "motivation_overall_building_solid"
        case is PlanMotivationMessageOverallProgressApproachingThird: "motivation_overall_approaching_third"
        case is PlanMotivationMessageOverallProgressPastThirty: "motivation_overall_past_thirty"
        case is PlanMotivationMessageOverallProgressHalfway: "motivation_overall_halfway"
        case is PlanMotivationMessageOverallProgressMoreThanHalf: "motivation_overall_more_than_half"
        case is PlanMotivationMessageOverallProgressThreeQuarters: "motivation_overall_three_quarters"
        case is PlanMotivationMessageOverallProgressFinalStretch: "motivation_overall_final_stretch"
        case is PlanMotivationMessageOverallProgressAlmostThere: "motivation_overall_almost_there"
        case is PlanMotivationMessageOverallProgressCompleted: "motivation_overall_completed"
        case is PlanMotivationMessageStreakDay1: "motivation_streak_1"
        case is PlanMotivationMessageStreakDay3: "motivation_streak_3"
        case is PlanMotivationMessageStreakDay7: "motivation_streak_7"
        case is PlanMotivationMessageStreakDay14: "motivation_streak_14"
        case is PlanMotivationMessageStreakDay30: "motivation_streak_30"
        case is PlanMotivationMessageStreakDay100: "motivation_streak_100"
        case is PlanMotivationMessageDaySituationNotStarted: "motivation_day_not_started"
        case is PlanMotivationMessageDaySituationStarted: "motivation_day_started"
        case is PlanMotivationMessageDaySituationCompleted: "motivation_day_completed"
        case is PlanMotivationMessageDaySituationOneOverdue: "motivation_day_one_overdue"
        case is PlanMotivationMessageDaySituationMultipleOverdue: "motivation_day_multiple_overdue"
        case is PlanMotivationMessageMilestoneEnteredNewTestament: "motivation_milestone_entered_new_testament"
        case is PlanMotivationMessageMilestoneFirstBookCompleted: "motivation_milestone_first_book_completed"
        default: "motivation_overall_zero"
        }
    }
}
