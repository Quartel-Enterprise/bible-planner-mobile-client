import ActivityKit
import AppIntents
import SwiftUI
import WidgetKit

// MARK: - Widget Declaration

struct BibleDownloadWidgetLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: BibleDownloadAttributes.self) { context in
            LockScreenLiveActivityView(context: context)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Link(destination: bibleVersionsURL) {
                        DynamicIslandLeadingView(context: context)
                    }
                }
                DynamicIslandExpandedRegion(.center) {
                    Link(destination: bibleVersionsURL) {
                        DynamicIslandCenterView(context: context)
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    DynamicIslandTrailingButtons(context: context)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    EmptyView()
                }
            } compactLeading: {
                CompactLeadingView(context: context)
            } compactTrailing: {
                CompactTrailingView(context: context)
            } minimal: {
                MinimalView(context: context)
            }
        }
    }
}

// MARK: - Shared Constants

private let bibleVersionsURL = URL(string: "bibleplanner://navigate/bible-versions")!

// MARK: - Extensions

@available(iOS 16.2, *)
private extension BibleDownloadState {
    var label: String {
        switch self {
        case .downloading: return "Downloading..."
        case .paused:      return "Paused"
        case .complete:    return "Ready!"
        }
    }
}

@available(iOS 16.2, *)
private extension BibleDownloadAttributes {
    func pauseIntent() -> PauseDownloadIntent {
        var i = PauseDownloadIntent()
        i.versionId = versionId
        return i
    }

    func resumeIntent() -> ResumeDownloadIntent {
        var i = ResumeDownloadIntent()
        i.versionId = versionId
        return i
    }

    func cancelIntent() -> CancelDownloadIntent {
        var i = CancelDownloadIntent()
        i.versionId = versionId
        return i
    }
}

// MARK: - Shared Action Buttons

@available(iOS 16.2, *)
private struct DownloadActionButtons: View {
    let context: ActivityViewContext<BibleDownloadAttributes>
    var buttonSize: CGFloat = 16

    private var spacing: CGFloat { buttonSize < 16 ? 6 : 10 }

    var body: some View {
        HStack(spacing: spacing) {
            if context.state.downloadState == .downloading {
                LiveActivityButton(
                    systemImage: "pause.fill",
                    tint: Color(hex: "B6C4FF"),
                    size: buttonSize,
                    intent: context.attributes.pauseIntent()
                )
            } else if context.state.downloadState == .paused {
                LiveActivityButton(
                    systemImage: "play.fill",
                    tint: Color(hex: "B6C4FF"),
                    size: buttonSize,
                    intent: context.attributes.resumeIntent()
                )
            }
            LiveActivityButton(
                systemImage: "xmark",
                tint: Color(hex: "FFB4AB"),
                size: buttonSize,
                intent: context.attributes.cancelIntent()
            )
        }
    }
}

// MARK: - Lock Screen View

@available(iOS 16.2, *)
struct LockScreenLiveActivityView: View {
    let context: ActivityViewContext<BibleDownloadAttributes>

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(context.attributes.versionId.uppercased())
                        .font(.system(size: 34, weight: .black))
                        .foregroundColor(Color(hex: "FFD700"))
                        .lineLimit(1)
                    Text(context.state.downloadState.label)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(Color(hex: "C6C6D0"))
                        .kerning(0.3)
                }
                Spacer()
                if context.state.downloadState != .complete {
                    DownloadActionButtons(context: context)
                }
            }

            Spacer(minLength: 12)

            VStack(spacing: 8) {
                HStack(alignment: .lastTextBaseline) {
                    Text("\(context.state.progressStr)%")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(Color(hex: "B6C4FF"))
                    Spacer()
                    if let label = context.state.estimatedTimeLabel {
                        Text(label.uppercased())
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(Color(hex: "C6C6D0"))
                            .kerning(1.5)
                    }
                }
                ProgressBarView(progress: context.state.progress)
            }
        }
        .padding(20)
        .background(Color(hex: "1E1F25"))
        .widgetURL(bibleVersionsURL)
    }
}

// MARK: - Dynamic Island: Expanded Leading

@available(iOS 16.2, *)
struct DynamicIslandLeadingView: View {
    let context: ActivityViewContext<BibleDownloadAttributes>

    var body: some View {
        ZStack {
            Circle()
                .stroke(Color(hex: "34343A"), lineWidth: 4)
                .frame(width: 44, height: 44)
            Circle()
                .trim(from: 0, to: context.state.progress)
                .stroke(
                    Color(hex: "B6C4FF"),
                    style: StrokeStyle(lineWidth: 4, lineCap: .round)
                )
                .frame(width: 44, height: 44)
                .rotationEffect(.degrees(-90))
            Text(context.attributes.versionId.uppercased())
                .font(.system(size: 9, weight: .black))
                .foregroundColor(Color(hex: "FFD700"))
                .lineLimit(1)
        }
        .padding(.leading, 4)
    }
}

// MARK: - Dynamic Island: Expanded Center

@available(iOS 16.2, *)
struct DynamicIslandCenterView: View {
    let context: ActivityViewContext<BibleDownloadAttributes>

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("\(context.state.progressStr)% Complete")
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(Color(hex: "B6C4FF"))
            Text(context.state.downloadState.label.uppercased())
                .font(.system(size: 10, weight: .medium))
                .foregroundColor(Color(hex: "C6C6D0"))
                .kerning(1.2)
                .opacity(0.85)
        }
    }
}

// MARK: - Dynamic Island: Expanded Trailing Buttons

@available(iOS 16.2, *)
struct DynamicIslandTrailingButtons: View {
    let context: ActivityViewContext<BibleDownloadAttributes>

    var body: some View {
        if context.state.downloadState != .complete {
            DownloadActionButtons(context: context, buttonSize: 13)
                .padding(.trailing, 4)
        }
    }
}

// MARK: - Dynamic Island: Compact Leading

@available(iOS 16.2, *)
struct CompactLeadingView: View {
    let context: ActivityViewContext<BibleDownloadAttributes>

    var body: some View {
        HStack(spacing: 4) {
            ZStack {
                Circle()
                    .stroke(Color(hex: "34343A"), lineWidth: 2.5)
                    .frame(width: 20, height: 20)
                Circle()
                    .trim(from: 0, to: context.state.progress)
                    .stroke(
                        Color(hex: "B6C4FF"),
                        style: StrokeStyle(lineWidth: 2.5, lineCap: .round)
                    )
                    .frame(width: 20, height: 20)
                    .rotationEffect(.degrees(-90))
            }
            Text(context.attributes.versionId.uppercased())
                .font(.system(size: 11, weight: .black))
                .foregroundColor(Color(hex: "FFD700"))
                .lineLimit(1)
        }
        .padding(.leading, 4)
        .widgetURL(bibleVersionsURL)
    }
}

// MARK: - Dynamic Island: Compact Trailing

@available(iOS 16.2, *)
struct CompactTrailingView: View {
    let context: ActivityViewContext<BibleDownloadAttributes>

    var body: some View {
        Text("\(context.state.progressStr)%")
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(Color(hex: "B6C4FF"))
            .padding(.trailing, 4)
            .widgetURL(bibleVersionsURL)
    }
}

// MARK: - Dynamic Island: Minimal

@available(iOS 16.2, *)
struct MinimalView: View {
    let context: ActivityViewContext<BibleDownloadAttributes>

    var body: some View {
        ZStack {
            Circle()
                .stroke(Color(hex: "34343A"), lineWidth: 2.5)
            Circle()
                .trim(from: 0, to: context.state.progress)
                .stroke(
                    Color(hex: "B6C4FF"),
                    style: StrokeStyle(lineWidth: 2.5, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
        }
        .padding(3)
        .widgetURL(bibleVersionsURL)
    }
}

// MARK: - Shared Components

struct ProgressBarView: View {
    let progress: Double

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                Capsule()
                    .fill(Color(hex: "45464F"))
                    .frame(height: 10)
                Capsule()
                    .fill(Color(hex: "B6C4FF"))
                    .frame(width: max(0, geo.size.width * progress), height: 10)
                    .animation(.easeInOut(duration: 0.6), value: progress)
            }
        }
        .frame(height: 10)
    }
}

struct LiveActivityButton<I: AppIntent>: View {
    let systemImage: String
    let tint: Color
    var size: CGFloat = 16
    let intent: I

    var body: some View {
        Button(intent: intent) {
            Image(systemName: systemImage)
                .font(.system(size: size, weight: .semibold))
                .foregroundColor(tint)
                .padding(size * 0.75)
                .background(tint.opacity(0.12))
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Helpers

extension Color {
    init(hex: String) {
        var hexStr = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        if hexStr.hasPrefix("#") { hexStr.removeFirst() }
        var rgb: UInt64 = 0
        Scanner(string: hexStr).scanHexInt64(&rgb)
        let r = Double((rgb >> 16) & 0xFF) / 255.0
        let g = Double((rgb >> 8) & 0xFF) / 255.0
        let b = Double(rgb & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}
