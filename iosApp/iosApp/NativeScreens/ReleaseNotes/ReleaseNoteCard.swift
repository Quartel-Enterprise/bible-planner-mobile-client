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
                if let tag = note.tag {
                    tagLabel(tag)
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

    private func tagLabel(_ tag: ReleaseNotesNativeTag) -> some View {
        Text(tag.text)
            .font(.caption2.bold())
            .foregroundStyle(Color(kotlinArgb: tag.contentArgb))
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color(kotlinArgb: tag.backgroundArgb), in: RoundedRectangle(cornerRadius: 4))
    }
}
