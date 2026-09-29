import SwiftUI

/// The tag's image gallery as a staggered grid (Android's `LazyVerticalStaggeredGrid` of
/// `TagGalleryImageItem`): columns at least 160 pt wide, each photo at its own aspect ratio clamped
/// to 0.65–1.8. Items go to the currently shortest column; the placement only depends on the items
/// before them, so loading another page never moves the photos already shown.
struct TagGallery: View {
    let items: [Resource]
    let open: (Resource) -> Void
    let loadMore: () -> Void
    @State private var width: CGFloat = 0
    @State private var viewer: ViewerImage?
    @Environment(\.mediaLoader) private var loader

    private static let minColumnWidth: CGFloat = 160
    private static let spacing: CGFloat = 8

    var body: some View {
        HStack(alignment: .top, spacing: Self.spacing) {
            ForEach(Array(columns.enumerated()), id: \.offset) { _, column in
                LazyVStack(spacing: Self.spacing) {
                    ForEach(column) { item in
                        TagGalleryTile(item: item, ratio: Self.ratio(item),
                                       showImage: { showImage(item) }, openEntry: { open(item) })
                            .onAppear { if item.id == items.last?.id { loadMore() } }
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .top)
        .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { width = $0 }
        .fullScreenCover(item: $viewer) { ImageViewerScreen(bytes: $0.bytes, key: $0.key) }
    }

    private var columnCount: Int {
        guard width > 0 else { return 2 }
        return max(2, Int((width + Self.spacing) / (Self.minColumnWidth + Self.spacing)))
    }

    private var columns: [[Resource]] {
        var columns = Array(repeating: [Resource](), count: columnCount)
        var heights = Array(repeating: CGFloat(0), count: columnCount)
        for item in items {
            let shortest = heights.indices.min { heights[$0] < heights[$1] } ?? 0
            columns[shortest].append(item)
            heights[shortest] += 1 / Self.ratio(item)
        }
        return columns
    }

    static func ratio(_ item: Resource) -> CGFloat {
        guard let photo = item.photo, photo.width > 0, photo.height > 0 else { return 1 }
        return min(1.8, max(0.65, CGFloat(photo.width) / CGFloat(photo.height)))
    }

    private func showImage(_ item: Resource) {
        guard let photo = item.photo, let loader else { return }
        Task {
            guard let bytes = try? await loader.bytes(for: photo.url) else { return }
            viewer = ViewerImage(bytes: bytes, key: photo.url)
        }
    }
}

private struct ViewerImage: Identifiable {
    let bytes: Data
    let key: String
    var id: String { key }
}

/// One gallery photo: cropped to its ratio (at least 120 pt tall) on a card, with 18+ and GIF
/// badges top left and an open-entry button top right.
private struct TagGalleryTile: View {
    let item: Resource
    let ratio: CGFloat
    let showImage: () -> Void
    let openEntry: () -> Void
    @Environment(\.adultContentAllowed) private var adultContentAllowed

    /// Without the account's +18 setting an adult photo is neither loaded nor opened (see
    /// `AdultContentGate`); the tile keeps its badge and the open-entry button.
    private var photoHidden: Bool { item.adult && !adultContentAllowed }

    var body: some View {
        Color.clear
            .aspectRatio(ratio, contentMode: .fit)
            .frame(minHeight: 120)
            .overlay {
                RemoteImage(url: photoHidden ? nil : item.photo?.url, maxDimension: 700) { PodkopTheme.cardInset }
            }
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .onTapGesture { if !photoHidden { showImage() } }
            .overlay(alignment: .topLeading) {
                HStack(spacing: 6) {
                    if item.adult { badge("18+") }
                    if item.photo?.isAnimated == true { badge("GIF") }
                }
                .padding(8)
            }
            .overlay(alignment: .topTrailing) {
                Button(action: openEntry) {
                    Image(systemName: "arrow.up.forward.square")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(width: 40, height: 40)
                        .background(.black.opacity(0.55), in: Circle())
                }
                .buttonStyle(.plain)
                .padding(6)
                .accessibilityLabel(.commonOpenEntry)
            }
            .accessibilityElement(children: .contain)
            .accessibilityAddTraits(.isImage)
            .accessibilityAction(named: Text(.contentOpensImage), showImage)
            .accessibilityIdentifier("galleryItem-\(item.sourceID)")
    }

    private func badge(_ text: String) -> some View {
        Text(verbatim: text)
            .font(.caption.weight(.bold))
            .foregroundStyle(.primary)
            .padding(.horizontal, 6).padding(.vertical, 2)
            .background(PodkopTheme.card.opacity(0.88), in: RoundedRectangle(cornerRadius: 4, style: .continuous))
    }
}
