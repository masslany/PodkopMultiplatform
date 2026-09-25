import Foundation
import PodkopShared

struct HitsArchive: Hashable {
    static let startYear = 2007
    static let startMonth = 12
    let year: Int
    let month: Int

    static func isAvailable(year: Int, month: Int, now: Date = Date(),
                            calendar: Calendar = .current) -> Bool {
        let current = calendar.dateComponents([.year, .month], from: now)
        guard let maxYear = current.year, let maxMonth = current.month,
              (1...12).contains(month), (startYear...maxYear).contains(year) else { return false }
        if year == startYear && month < startMonth { return false }
        if year == maxYear && month > maxMonth { return false }
        return true
    }
}

struct RankUser: Identifiable, Equatable {
    let username: String
    var avatarURL: String? = nil
    let color: String
    let gender: String
    let memberSince: String?
    let position: Int
    let trend: Int
    let actions: Int
    let links: Int
    let entries: Int
    let followers: Int
    var id: String { username }
}

struct ObservedItem: Identifiable {
    let resource: Resource
    let newContentCount: Int?
    var id: String { "\(resource.id):\(resource.parentID ?? 0)" }
}
