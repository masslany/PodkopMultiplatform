import SwiftUI

/// A hit in the horizontal strip above the links feed: the link image with its title over a
/// dark gradient and the vote count in the corner (Android's `HitItem`).
struct HitTile: View {
    let resource: Resource
    let open: () -> Void

    var body: some View {
        Button(action: open) {
            ZStack(alignment: .bottomLeading) {
                RemoteImage(url: resource.photo?.url, maxDimension: 480) {
                    LinearGradient(colors: [.gray, .gray.opacity(0.6)],
                                   startPoint: .topLeading, endPoint: .bottomTrailing)
                }
                .frame(width: 180, height: 124)
                .blur(radius: resource.adult ? 16 : 0, opaque: true)
                LinearGradient(colors: [.clear, .black.opacity(0.85)], startPoint: .center, endPoint: .bottom)
                Text(resource.title.isEmpty ? resource.body : resource.title)
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(.white)
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
                    .padding(8)
            }
            .frame(width: 180, height: 124)
            .overlay(alignment: .topLeading) {
                HStack(spacing: 4) {
                    Text(resource.vote.up.formatted())
                        .font(.caption.weight(.bold).monospacedDigit())
                        .foregroundStyle(.white)
                        .padding(.horizontal, 7).padding(.vertical, 3)
                        .background(WykopTheme.hotOrange, in: RoundedRectangle(cornerRadius: 6, style: .continuous))
                    if resource.adult {
                        Text(verbatim: "18+")
                            .font(.caption2.weight(.heavy))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 5).padding(.vertical, 3)
                            .background(WykopTheme.adultRed, in: RoundedRectangle(cornerRadius: 6, style: .continuous))
                    }
                }
                .padding(8)
            }
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(resource.title)
        .accessibilityValue(String(localized: .commonVotes(resource.vote.up)))
        .accessibilityAddTraits(.isButton)
    }
}
