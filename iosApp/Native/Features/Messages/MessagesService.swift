import Foundation
import Observation
import PodkopShared

@MainActor protocol MessagesLoading {
    func normalize(_ username: String) -> String
    func firstRequest() -> FeedRequest
    func conversations(request: FeedRequest, loaded: Int) async throws -> ListPage<NativeConversation>
    func thread(_ username: String, request: FeedRequest, loaded: Int) async throws -> ListPage<NativeMessage>
    func newer(_ username: String) async throws -> [NativeMessage]
    func send(_ username: String, text: String, adult: Bool, photoKey: String?) async throws -> NativeMessage
    func readAll() async throws
    func refreshStatus() async throws
}

@MainActor
final class SharedMessagesLoader: MessagesLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    private func bridge(_ request: FeedRequest) -> IOSPageRequest {
        IOSPageRequest(kind: request.kind, value: request.value)
    }

    func normalize(_ username: String) -> String { client.messages.normalizeUsername(value: username) }

    func firstRequest() -> FeedRequest { FeedRequest(client.messages.firstRequest()) }

    func conversations(request: FeedRequest, loaded: Int) async throws -> ListPage<NativeConversation> {
        let page: IOSConversationPage = try await adapter.call {
            self.client.messages.conversations(request: self.bridge(request), loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map {
            NativeConversation(username: $0.username, avatarURL: $0.avatarUrl, color: $0.color, gender: $0.gender,
                               lastMessage: $0.lastMessage, lastMessageAt: NativeDates.parse($0.lastMessageAt),
                               unread: $0.unread)
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }

    func thread(_ username: String, request: FeedRequest, loaded: Int) async throws -> ListPage<NativeMessage> {
        let page: IOSMessagePage = try await adapter.call {
            self.client.messages.thread(username: username, request: self.bridge(request),
                                        loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map(NativeMessage.init), next: page.next.map(FeedRequest.init),
                        total: page.total?.intValue)
    }

    func newer(_ username: String) async throws -> [NativeMessage] {
        let values: [IOSPrivateMessage] = try await adapter.call {
            self.client.messages.newer(username: username, completion: $0)
        }
        return values.map(NativeMessage.init)
    }

    func send(_ username: String, text: String, adult: Bool, photoKey: String?) async throws -> NativeMessage {
        let value: IOSPrivateMessage = try await adapter.call {
            self.client.messages.send(username: username, content: text, adult: adult, photoKey: photoKey,
                                      completion: $0)
        }
        return NativeMessage(value)
    }

    func readAll() async throws {
        let _: IOSSuccess = try await adapter.call { self.client.messages.readAll(completion: $0) }
    }

    func refreshStatus() async throws {
        let _: IOSNotificationStatus = try await adapter.call { self.client.notifications.refreshStatus(completion: $0) }
    }
}

extension NativeMessage {
    init(_ value: IOSPrivateMessage) {
        self.init(key: value.key, content: value.content,
                  createdAt: Date(timeIntervalSince1970: Double(value.createdAtEpochMillis) / 1000),
                  incoming: value.incoming, adult: value.adult, sender: value.senderUsername,
                  senderColor: value.senderColor,
                  photo: value.photo.map { NativePhoto(url: $0.url, width: Int($0.width), height: Int($0.height),
                                                       mimeType: $0.mimeType, key: $0.key) },
                  embedURL: value.embedUrl)
    }
}
