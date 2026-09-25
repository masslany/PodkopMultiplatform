import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureMessagesLoader: MessagesLoading {
    private var sent: [NativeMessage] = []
    func normalize(_ username: String) -> String {
        let trimmed = username.trimmingCharacters(in: .whitespaces)
        return trimmed.hasPrefix("@") ? String(trimmed.dropFirst()) : trimmed
    }
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func conversations(request: FeedRequest, loaded: Int) async throws -> ListPage<NativeConversation> {
        ListPage(items: [NativeConversation(username: "Ewa-Żółw", color: "green", gender: "female",
                                            lastMessage: "Do zobaczenia!", lastMessageAt: Date(), unread: true)],
                 next: nil, total: 1)
    }
    func thread(_ username: String, request: FeedRequest, loaded: Int) async throws -> ListPage<NativeMessage> {
        if username == "nieistnieje" { throw BridgeFailure(category: "notFound", code: "404") }
        let base = Date(timeIntervalSince1970: 1_780_000_000)
        let items = [
            NativeMessage(key: "m1", content: "Cześć! 👋", createdAt: base, incoming: true, adult: false,
                          sender: username, senderColor: "green", photo: nil, embedURL: nil),
            NativeMessage(key: "m2", content: "Hej, co słychać?", createdAt: base.addingTimeInterval(60),
                          incoming: false, adult: false, sender: "Ja", senderColor: "orange", photo: nil, embedURL: nil),
        ]
        return ListPage(items: items + sent, next: nil, total: 2 + sent.count)
    }
    func newer(_ username: String) async throws -> [NativeMessage] { [] }
    func send(_ username: String, text: String, adult: Bool, photoKey: String?) async throws -> NativeMessage {
        let message = NativeMessage(key: "sent-\(sent.count)", content: text, createdAt: Date(), incoming: false,
                                    adult: adult, sender: "Ja", senderColor: "orange", photo: nil, embedURL: nil)
        sent.append(message)
        return message
    }
    func readAll() async throws {}
    func refreshStatus() async throws {}
}
#endif
