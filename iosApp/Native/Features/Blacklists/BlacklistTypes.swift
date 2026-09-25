import Foundation
import Observation
import PodkopShared

enum BlacklistCategory: String, CaseIterable {
    case users, tags, domains
}

struct BlacklistEntry: Identifiable, Equatable {
    let category: BlacklistCategory
    /// Normalized value used for removal and routing.
    let value: String
    let color: String?
    let gender: String?
    var avatarURL: String? = nil
    var id: String { "\(category.rawValue):\(value)" }
    var label: String { category == .tags ? "#\(value)" : value }
}
