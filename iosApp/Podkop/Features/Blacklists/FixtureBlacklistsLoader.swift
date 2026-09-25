import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureBlacklistsLoader: BlacklistsLoading {
    private var values: [BlacklistCategory: [String]] = [.users: ["spamer"], .tags: ["polityka"], .domains: ["example.com"]]
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func normalize(_ category: BlacklistCategory, _ value: String) -> String {
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        switch category {
        case .users: return trimmed.hasPrefix("@") ? String(trimmed.dropFirst()) : trimmed
        case .tags: return (trimmed.hasPrefix("#") ? String(trimmed.dropFirst()) : trimmed).lowercased()
        case .domains: return trimmed.lowercased()
        }
    }
    func load(_ category: BlacklistCategory, request: FeedRequest, loaded: Int) async throws
        -> ListPage<BlacklistEntry> {
        let items = values[category, default: []].map {
            BlacklistEntry(category: category, value: $0, color: category == .users ? "green" : nil, gender: nil)
        }
        return ListPage(items: items, next: nil, total: items.count)
    }
    func add(_ category: BlacklistCategory, _ value: String) async throws { values[category, default: []].append(value) }
    func remove(_ entry: BlacklistEntry) async throws { values[entry.category]?.removeAll { $0 == entry.value } }
}
#endif
