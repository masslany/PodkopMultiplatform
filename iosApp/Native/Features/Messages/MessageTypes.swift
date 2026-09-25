import Foundation
import PodkopShared

struct NativeConversation: Identifiable, Equatable {
    let username: String
    var avatarURL: String? = nil
    let color: String
    let gender: String
    let lastMessage: String?
    let lastMessageAt: Date?
    let unread: Bool
    var id: String { username }
}

struct NativeMessage: Identifiable, Equatable {
    let key: String
    let content: String?
    let createdAt: Date
    let incoming: Bool
    let adult: Bool
    let sender: String?
    let senderColor: String?
    let photo: NativePhoto?
    let embedURL: String?
    var id: String { key }

    /// Android's merge: newer copies replace older ones by key, ordered by time then key.
    static func merge(_ existing: [NativeMessage], _ incoming: [NativeMessage]) -> [NativeMessage] {
        var byKey: [String: NativeMessage] = [:]
        for message in existing { byKey[message.key] = message }
        for message in incoming { byKey[message.key] = message }
        return byKey.values.sorted { lhs, rhs in
            lhs.createdAt == rhs.createdAt ? lhs.key < rhs.key : lhs.createdAt < rhs.createdAt
        }
    }
}
