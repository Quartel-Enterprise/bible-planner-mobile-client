import Shared
import SwiftUI

/// Today's reading, or where to pick up, with the buttons to read it or mark it as read.
struct ReadingPlanHeroCard: View {
    let status: PlanStatus
    let strings: ReadingPlanText
    let onEvent: (ReadingPlanUiEvent) -> Void

    var body: some View {
        let mode = PlanModeKind(status.mode)
        switch mode {
        case .done:
            doneContent
        case .behind:
            if let next = status.nextDay {
                behindContent(next: next)
            }
        case .new, .onTrack, .caughtUp, .ahead:
            if let next = status.nextDay {
                standardContent(next: next, mode: mode)
            }
        }
    }

    private func standardContent(next: PlanDayRef, mode: PlanModeKind) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .firstTextBaseline) {
                kicker(mode: mode)
                Spacer(minLength: 8)
                // Ahead and caught up show a later day, so their date would read as today's.
                if mode != .ahead, mode != .caughtUp, let date = planDate(next) {
                    Text(date.shortText(locale: strings.locale))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
            passageText(next)
            Text(strings.heroDayText(mode, dayIndex: next.globalIndex, totalDays: status.totalDays))
                .font(.subheadline)
            if let note = strings.heroNote(mode, daysAhead: status.daysAhead) {
                Text(note)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            HStack(spacing: 12) {
                Button {
                    onEvent(ReadingPlanUiEventOnDayClick(dayNumber: next.dayNumber, weekNumber: next.weekNumber))
                } label: {
                    Label(strings.heroPrimaryTitle(mode), systemImage: "arrow.forward")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                Button {
                    onEvent(ReadingPlanUiEventOnDayReadClick(dayNumber: next.dayNumber, weekNumber: next.weekNumber))
                } label: {
                    Image(systemName: "checkmark")
                }
                .buttonStyle(.bordered)
                .accessibilityLabel(strings.text("mark_as_read"))
            }
            .controlSize(.large)
            .padding(.top, 6)
        }
        .padding(.vertical, 8)
    }

    private func behindContent(next: PlanDayRef) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            kicker(mode: .behind)
            passageText(next)
            Text(
                strings.format(
                    "hero_behind_day",
                    Int(next.globalIndex),
                    planDate(next)?.shortText(locale: strings.locale) ?? ""
                )
            )
            .font(.subheadline)
            Text(strings.format("days_behind_reassure", Int(status.daysBehind)))
                .font(.footnote)
                .foregroundStyle(.secondary)
            Button {
                onEvent(ReadingPlanUiEventOnResumeReadingClick(dayNumber: next.dayNumber, weekNumber: next.weekNumber))
            } label: {
                Label(strings.heroPrimaryTitle(.behind), systemImage: "play.fill")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .padding(.top, 6)
            if let today = status.todayDay {
                Button {
                    onEvent(ReadingPlanUiEventOnSkipToTodayClick.shared)
                } label: {
                    Text(strings.format("hero_skip_to_today", Int(today.globalIndex), strings.readingLabel(today.passages)))
                        .font(.subheadline)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderless)
            }
        }
        .padding(.vertical, 8)
    }

    private var doneContent: some View {
        VStack(spacing: 6) {
            Image(systemName: "trophy.fill")
                .font(.largeTitle)
                .foregroundStyle(.tint)
                .accessibilityHidden(true)
            Text(strings.text("done_title"))
                .font(.title2.bold())
            Text(strings.format("done_subtitle", Int(status.totalDays)))
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
    }

    @ViewBuilder
    private func kicker(mode: PlanModeKind) -> some View {
        let title = strings.heroKicker(mode)
        Group {
            if let symbol = strings.heroKickerSymbol(mode) {
                Label(title, systemImage: symbol)
            } else {
                Text(title)
            }
        }
        .font(.subheadline.weight(.semibold))
        .foregroundStyle(.tint)
    }

    private func passageText(_ day: PlanDayRef) -> some View {
        Text(strings.readingLabel(day.passages))
            .font(.title2.bold())
    }

    private func planDate(_ day: PlanDayRef) -> PlanDate? {
        day.plannedReadDate.flatMap { PlanDate(kotlinIsoDate: $0.description) }
    }
}
