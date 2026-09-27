import Foundation
import PodkopShared

struct Conversation: Identifiable, Equatable {
    let username: String
    var avatarURL: String? = nil
    let color: String
    let gender: String
    let lastMessage: String?
    let lastMessageAt: Date?
    let unread: Bool
    var id: String { username }
}

struct Message: Identifiable, Equatable {
    let key: String
    let content: String?
    let createdAt: Date
    let incoming: Bool
    let adult: Bool
    let sender: String?
    let senderColor: String?
    let photo: Photo?
    let embedURL: String?
    var id: String { key }

    /// Android's merge: newer copies replace older ones by key, ordered by time then key.
    static func merge(_ existing: [Message], _ incoming: [Message]) -> [Message] {
        var byKey: [String: Message] = [:]
        for message in existing { byKey[message.key] = message }
        for message in incoming { byKey[message.key] = message }
        return byKey.values.sorted { lhs, rhs in
            lhs.createdAt == rhs.createdAt ? lhs.key < rhs.key : lhs.createdAt < rhs.createdAt
        }
    }
}
