import Shared
import SwiftUI

/// How much of the plan is read, the streak or the lapse, and a line of motivation.
struct ReadingPlanProgressRow: View {
    let state: ReadingPlanUiStateLoaded
    let strings: ReadingPlanText

    @ScaledMetric(relativeTo: .title3) private var ringSize: CGFloat = 76

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 16) {
                ReadingPlanProgressRing(fraction: Double(state.progress) / 100, label: percentText)
                    .frame(width: ringSize, height: ringSize)
                VStack(alignment: .leading, spacing: 4) {
                    Text(strings.format("progress_days_completed", Int(state.readDaysCount), Int(state.totalDaysCount)))
                        .font(.headline)
                    Text(strings.format("progress_days_remaining", max(Int(state.totalDaysCount) - Int(state.readDaysCount), 0)))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    statusLabel
                }
            }
            Text(strings.motivation(state.motivationMessage))
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 6)
    }

    private var percentText: String {
        (Double(state.progress) / 100).formatted(.percent.precision(.fractionLength(0...2)).locale(strings.locale))
    }

    @ViewBuilder
    private var statusLabel: some View {
        let status = state.planStatus
        if PlanModeKind(status.mode) == .behind, let daysSinceLastRead = status.daysSinceLastRead {
            Label(strings.format("no_read_for_days", Int(daysSinceLastRead.int32Value)), systemImage: "clock")
                .font(.footnote)
                .foregroundStyle(.secondary)
        } else if status.shownStreakDays > 0 {
            Label(strings.format("streak_days_label", Int(status.shownStreakDays)), systemImage: "flame.fill")
                .font(.footnote)
                .foregroundStyle(.orange)
        }
    }
}

private struct ReadingPlanProgressRing: View {
    let fraction: Double
    let label: String

    var body: some View {
        ZStack {
            Circle()
                .stroke(Color.secondary.opacity(0.2), lineWidth: 8)
            Circle()
                .trim(from: 0, to: min(max(fraction, 0), 1))
                .stroke(Color.accentColor, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                .rotationEffect(.degrees(-90))
            Text(label)
                .font(.subheadline.weight(.semibold))
                .monospacedDigit()
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(12)
        }
        .animation(.easeInOut, value: fraction)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(label)
    }
}
