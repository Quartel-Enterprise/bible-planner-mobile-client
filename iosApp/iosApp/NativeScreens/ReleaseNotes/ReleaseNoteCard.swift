import Shared
import SwiftUI

struct ReleaseNoteCard: View {
    let note: ReleaseNotesNativeNote
    let colors: ReleaseNotesNativeColors
    let onOpen: () -> Void

    var body: some View {
        // Upcoming versions have no GitHub release page yet, so their card is not tappable.
        if note.isUpcoming {
            card
        } else {
            Button(action: onOpen) { card }
                .buttonStyle(.plain)
        }
    }

    private var card: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(note.version)
                        .font(.title2.bold())
                        .foregroundStyle(Color(kotlinArgb: colors.onCardArgb))
                    if let dateText = note.dateText {
                        Text(dateText)
                            .font(.caption)
                            .foregroundStyle(Color(kotlinArgb: colors.onCardVariantArgb))
                    }
                }
                Spacer()
                if let tagText = note.tagText {
                    tag(text: tagText)
                }
            }
            VStack(alignment: .leading, spacing: 8) {
                ForEach(Array(note.changes.enumerated()), id: \.offset) { _, change in
                    HStack(alignment: .firstTextBaseline, spacing: 8) {
                        Text("•")
                            .foregroundStyle(Color(kotlinArgb: colors.primaryArgb))
                        Text(change)
                            .foregroundStyle(Color(kotlinArgb: colors.onCardVariantArgb))
                    }
                    .font(.callout)
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(kotlinArgb: colors.cardArgb), in: RoundedRectangle(cornerRadius: 12))
        .contentShape(RoundedRectangle(cornerRadius: 12))
    }

    private func tag(text: String) -> some View {
        let background = note.isUpcoming ? colors.upcomingTagBackgroundArgb : colors.latestTagBackgroundArgb
        let content = note.isUpcoming ? colors.upcomingTagContentArgb : colors.latestTagContentArgb
        return Text(text)
            .font(.caption2.bold())
            .foregroundStyle(Color(kotlinArgb: content))
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color(kotlinArgb: background), in: RoundedRectangle(cornerRadius: 4))
    }
}
