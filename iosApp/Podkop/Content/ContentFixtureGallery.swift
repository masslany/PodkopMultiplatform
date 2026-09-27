import SwiftUI

struct ContentFixtureGallery: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    TweetCard(tweet: TweetPreview(
                        authorName: "Lukáš Valášek", handle: "lukasvalasek", avatarURL: "https://example.com/a.png",
                        text: "❗ Chytili jsme v Praze do pasti propagandisty ruského centra Rybar. https://t.co/SMPKKhqgTq",
                        replies: 253, reposts: 1200, likes: 9145,
                        mediaThumbnailURL: "https://example.com/m.png", mediaAspectRatio: 1.6),
                              url: URL(string: "https://x.com/lukasvalasek/status/1"), open: { _ in })
                    TweetPlaceholderCard(failed: false, url: URL(string: "https://x.com/a/status/1"))
                    TweetPlaceholderCard(failed: true, url: URL(string: "https://x.com/a/status/1"))
                    ForEach(ContentFixtures.all) { resource in
                        ResourceCard(resource: resource)
                    }
                }
                .padding()
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)
            }
            .navigationTitle(.commonContent)
        }
    }
}
