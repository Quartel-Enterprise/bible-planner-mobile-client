import Shared
import SwiftUI

/// A week that expands into its days.
struct ReadingPlanWeekRow: View {
    let week: WeekPlanPresentationModel
    let flashTargetGlobalIndex: Int32
    let strings: ReadingPlanText
    let onEvent: (ReadingPlanUiEvent) -> Void
    let rowID: String
    /// Called as the week's label or one of its days scrolls in or out, so the week counts as on screen while any
    /// of its rows is, like the whole week card of the shared screen.
    let onRowVisibilityChange: (Bool) -> Void

    var body: some View {
        DisclosureGroup(isExpanded: expansion) {
            ForEach(week.dayPlans, id: \.globalDayIndex) { dayPlan in
                ReadingPlanDayRow(
                    weekNumber: week.weekPlan.number,
                    dayPlan: dayPlan,
                    isFlashing: flashTargetGlobalIndex != 0 && dayPlan.globalDayIndex == flashTargetGlobalIndex,
                    strings: strings,
                    onEvent: onEvent
                )
                .onAppear { onRowVisibilityChange(true) }
                .onDisappear { onRowVisibilityChange(false) }
            }
        } label: {
            label
                .id(rowID)
                .onAppear { onRowVisibilityChange(true) }
                .onDisappear { onRowVisibilityChange(false) }
        }
    }

    private var expansion: Binding<Bool> {
        let shownWeek = week
        return Binding(
            get: { shownWeek.isExpanded },
            set: { isExpanded in
                if isExpanded != shownWeek.isExpanded {
                    onEvent(ReadingPlanUiEventOnWeekExpandClick(weekNumber: shownWeek.weekPlan.number))
                }
            }
        )
    }

    private var label: some View {
        let group = WeekGroupKind(week.group)
        return HStack(spacing: 12) {
            if group == .completed {
                Image(systemName: "checkmark.circle.fill")
                    .font(.title2)
                    .foregroundStyle(.green)
                    .accessibilityHidden(true)
            }
            VStack(alignment: .leading, spacing: 4) {
                Text(strings.format("week_number", Int(week.weekPlan.number)))
                    .font(.headline)
                    .foregroundStyle(group == .current ? Color.primary : Color.secondary)
                HStack(spacing: 12) {
                    Text(strings.format("week_progress_summary", Int(week.readDaysCount), Int(week.totalDays)))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    if group == .current {
                        ProgressView(value: Double(week.readDaysCount), total: Double(max(week.totalDays, 1)))
                    }
                }
            }
        }
        .padding(.vertical, 4)
    }
}
