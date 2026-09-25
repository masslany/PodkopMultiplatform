import Foundation
import Observation
import PodkopShared

struct FeedRequest: Hashable {
    let kind: String
    let value: String?
    init(_ value: IOSPageRequest) { kind = value.kind; self.value = value.value }
    init(kind: String, value: String? = nil) { self.kind = kind; self.value = value }
}

struct FeedPolicy {
    let kind: String
    let initial: FeedRequest
}

struct FeedPage {
    let items: [Resource]
    let next: String?
    let total: Int?
}

struct FeedQuery: Equatable {
    let tab: AppTab
    var loggedIn: Bool
    var sort: String
    var hotHours: Int = 12
}
