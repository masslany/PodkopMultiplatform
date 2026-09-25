import Foundation
import Observation
import PodkopShared

struct AdvancedSearchForm: Equatable {
    enum Sort: String, CaseIterable { case score, popular, comments, newest }
    enum DatePreset: String, CaseIterable {
        case anyTime, last24Hours, last3Days, last7Days, last30Days, lastYear, custom
    }
    static let voteOptions: [Int?] = [nil, 50, 100, 500, 1000]

    var query = ""
    var sort: Sort = .score
    var minimumVotes: Int?
    var datePreset: DatePreset = .anyTime
    var customDateFrom = ""
    var customDateTo = ""
    var tags = ""
    var users = ""
    var domains = ""
    var category = ""
}

enum AdvancedSearchValidation: String, Error, Equatable {
    case queryRequired, invalidCustomDateFormat, invalidCustomDateRange, invalid
}

/// Opaque request built and validated by the shared layer.
struct AdvancedSearchRequest {
    let value: IOSSearchQuery?
}
