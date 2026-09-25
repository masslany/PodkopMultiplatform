import SwiftUI

enum ContentFixtures {
    static let author = NativeAuthor(name: "Ewa-Żółw", avatarURL: nil,
                                     color: "green", verified: true, online: true, rank: 24)

    static let link = NativeResource(
        sourceID: 101, kind: .link, title: "Przykładowy link o długim tytule",
        body: "Krótki opis z @ewa-test, #technologia i [odnośnikiem](https://example.com).\n-------------",
        author: author, commentCount: 12,
        vote: NativeVote(up: 58, down: 4, state: "positive", canUp: true,
                         canDown: false, canUndo: true),
        tags: ["technologia", "nauka"], sourceURL: "https://example.com",
        sourceLabel: "example.com", hot: true, slug: "sample-link")

    static let entry = NativeResource(
        sourceID: 102, kind: .entry, body: "Pierwszy akapit z emoji 👩🏽‍💻 i łączonymi znakami é.\n!Ukryty tekst ze spoilerem.\n- Pierwszy punkt\n- Drugi punkt\n> Cytat\n```swift\nlet a = 1\n```",
        author: author, commentCount: 3,
        vote: NativeVote(up: 8, down: 0, state: "none", canUp: true,
                         canDown: false, canUndo: false),
        survey: NativeSurvey(
            question: "Która odpowiedź?",
            answers: [.init(id: 1, text: "Pierwsza", count: 2, selected: false),
                      .init(id: 2, text: "Druga", count: 3, selected: true)],
            count: 5, canVote: false, selectedOption: 2
        ))

    static let entryComment = NativeResource(
        sourceID: 103, kind: .entryComment, body: "", author: author,
        deletion: .moderator, parentID: 102)

    static let linkComment = NativeResource(
        sourceID: 104, kind: .linkComment,
        body: "Treść tylko dla dorosłych z wieloma zdaniami.", author: author,
        adult: true, favourite: true, parentID: 101,
        vote: NativeVote(up: 2, down: 1, state: "negative", canUp: true,
                         canDown: true, canUndo: true))

    static let embed = NativeResource(
        sourceID: 105, kind: .entry,
        body: "Zewnętrzny materiał z uszkodzonym podglądem.", author: author,
        embed: NativeEmbed(key: "preview", url: "https://example.com/video",
                           thumbnailURL: "", type: "other"))

    static let all = [link, entry, entryComment, linkComment, embed]
}

struct ContentFixtureGallery: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(ContentFixtures.all) { resource in
                        NativeResourceCard(resource: resource)
                    }
                }
                .padding()
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)
            }
            .navigationTitle("Content")
        }
    }
}
