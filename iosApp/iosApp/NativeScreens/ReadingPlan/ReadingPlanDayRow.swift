import Shared
import SwiftUI

/// One day of a week: its read toggle, planned date, passages, and a tap that opens it.
struct ReadingPlanDayRow: View {
    let weekNumber: Int32
    let dayPlan: DayPlanPresentationModel
    let isFlashing: Bool
    let strings: ReadingPlanText
    let onEvent: (ReadingPlanUiEvent) -> Void

    @ScaledMetric(relativeTo: .headline) private var dateColumnWidth: CGFloat = 40
    @State private var flashOpacity: Double = 0

    var body: some View {
        let day = dayPlan.day
        HStack(spacing: 12) {
            readToggle(day: day)
            Button {
                onEvent(ReadingPlanUiEventOnDayClick(dayNumber: day.number, weekNumber: weekNumber))
            } label: {
                HStack(spacing: 12) {
                    if let date = day.plannedReadDate.flatMap({ PlanDate(kotlinIsoDate: $0.description) }) {
                        dateColumn(date: date, isRead: day.isRead)
                    }
                    textColumn(day: day)
                    Spacer(minLength: 0)
                    Image(systemName: "chevron.forward")
                        .font(.footnote.weight(.semibold))
                        .foregroundStyle(.tertiary)
                        .accessibilityHidden(true)
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
        .listRowBackground(background)
        .onChange(of: isFlashing, initial: true) { _, isFlashing in
            if isFlashing {
                flash()
            }
        }
    }

    private func readToggle(day: DayModel) -> some View {
        Button {
            onEvent(ReadingPlanUiEventOnDayReadClick(dayNumber: day.number, weekNumber: weekNumber))
        } label: {
            Image(systemName: day.isRead ? "checkmark.circle.fill" : "circle")
                .font(.title2)
                .foregroundStyle(day.isRead || dayPlan.isAccented ? Color.accentColor : Color.secondary)
                .contentTransition(.symbolEffect(.replace))
        }
        .buttonStyle(.borderless)
        .accessibilityLabel(strings.format("day_read", Int(dayPlan.globalDayIndex)))
        .accessibilityAddTraits(day.isRead ? [.isSelected] : [])
    }

    private func dateColumn(date: PlanDate, isRead: Bool) -> some View {
        let color = dateColor(isRead: isRead)
        return VStack(spacing: 0) {
            Text(date.dayText(locale: strings.locale))
                .font(.headline)
                .strikethrough(isRead)
            Text(date.monthText(locale: strings.locale))
                .font(.caption2)
                .strikethrough(isRead)
            if dayPlan.shouldShowYear {
                Text(date.yearText(locale: strings.locale))
                    .font(.caption2)
                    .strikethrough(isRead)
            }
        }
        .foregroundStyle(color)
        .frame(minWidth: dateColumnWidth)
    }

    private func dateColor(isRead: Bool) -> Color {
        if isRead { return .secondary }
        if dayPlan.isOverdue { return .orange }
        if dayPlan.isActive { return .accentColor }
        return .primary
    }

    private func textColumn(day: DayModel) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            HStack(spacing: 6) {
                Text(strings.format("day_number", Int(dayPlan.globalDayIndex)))
                    .font(.body.weight(.medium))
                    .strikethrough(day.isRead)
                    .foregroundStyle(day.isRead ? Color.secondary : Color.primary)
                if day.isToday && !day.isRead {
                    chip(title: strings.text("today_badge"), color: .accentColor)
                } else if dayPlan.isOverdue {
                    chip(title: strings.text("chip_overdue"), color: .orange)
                }
            }
            Text(strings.readingLabel(day.passages))
                .font(.subheadline)
                .strikethrough(day.isRead)
                .foregroundStyle(.secondary)
        }
    }

    private func chip(title: String, color: Color) -> some View {
        Text(title)
            .font(.caption2.weight(.semibold))
            .foregroundStyle(color)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(color.opacity(0.15), in: Capsule())
    }

    private var background: some View {
        ZStack {
            Color(uiColor: .secondarySystemGroupedBackground)
            if dayPlan.isActive {
                Color.accentColor.opacity(0.12)
            }
            Color.accentColor.opacity(flashOpacity)
        }
    }

    /// Flashes the row the shortcut or "skip to today" scrolled to, then tells the ViewModel it's done.
    private func flash() {
        withAnimation(.easeIn(duration: 0.15)) {
            flashOpacity = 0.3
        } completion: {
            withAnimation(.easeOut(duration: 1.2)) {
                flashOpacity = 0
            } completion: {
                onEvent(ReadingPlanUiEventOnFlashCompleted.shared)
            }
        }
    }
}
