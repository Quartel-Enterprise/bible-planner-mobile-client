import Shared
import SwiftUI

struct ReleaseNotesView: View {
    let model: ReleaseNotesScreenModel
    let onEvent: (ReleaseNotesUiEvent) -> Void

    var body: some View {
        if let state = model.state {
            screen(state: state)
        }
    }

    private func screen(state: ReleaseNotesNativeState) -> some View {
        let colors = state.colors
        return ReleaseNotesContentView(
            content: state.content,
            tabsLabel: state.title,
            colors: colors,
            onEvent: onEvent
        )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color(kotlinArgb: colors.backgroundArgb))
            .navigationTitle(state.title)
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        onEvent(ReleaseNotesUiEventOnBackClicked.shared)
                    } label: {
                        Image(systemName: "chevron.backward")
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        onEvent(ReleaseNotesUiEventOnGithubAllReleasesClicked.shared)
                    } label: {
                        Image(systemName: "arrow.up.right.square")
                    }
                    .accessibilityLabel(state.githubButtonLabel)
                }
            }
            .tint(Color(kotlinArgb: colors.primaryArgb))
    }
}

private struct ReleaseNotesContentView: View {
    /// Same cap as the Compose `ResponsiveColumn`, so cards don't stretch across an iPad.
    private let maxContentWidth: CGFloat = 800

    let content: ReleaseNotesNativeContent
    let tabsLabel: String
    let colors: ReleaseNotesNativeColors
    let onEvent: (ReleaseNotesUiEvent) -> Void

    var body: some View {
        if let success = content as? ReleaseNotesNativeContentSuccess {
            ScrollView {
                LazyVStack(spacing: 16) {
                    tabPicker(tabs: success.tabs)
                    ForEach(success.notes, id: \.version) { note in
                        ReleaseNoteCard(note: note, colors: colors) {
                            onEvent(ReleaseNotesUiEventOnGithubVersionClicked(version: note.version))
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .frame(maxWidth: maxContentWidth)
                .frame(maxWidth: .infinity)
            }
        } else if let error = content as? ReleaseNotesNativeContentError {
            Text(error.message)
                .font(.callout)
                .foregroundStyle(Color(kotlinArgb: colors.errorArgb))
                .multilineTextAlignment(.center)
                .padding(32)
        } else {
            ProgressView()
        }
    }

    @ViewBuilder
    private func tabPicker(tabs: [ReleaseNotesNativeTab]) -> some View {
        // Same fallback as the Compose tab row: with no selected tab, the first one is shown as selected.
        if let selectedTab = (tabs.first(where: { $0.isSelected }) ?? tabs.first)?.tab {
            let selection = Binding<ReleaseNotesTab>(
                get: { selectedTab },
                set: { tab in onEvent(ReleaseNotesUiEventOnTabSelected(tab: tab)) }
            )
            Picker(tabsLabel, selection: selection) {
                ForEach(tabs, id: \.tab) { tab in
                    Text(tab.title).tag(tab.tab)
                }
            }
            .pickerStyle(.segmented)
            .labelsHidden()
        }
    }
}
