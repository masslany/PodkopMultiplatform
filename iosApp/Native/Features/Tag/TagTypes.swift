import Foundation
import Observation
import PodkopShared

struct NativeTagDetails: Equatable {
    var name: String
    var description: String
    var followers: Int
    var bannerURL: String?
    var observed: Bool
    var notificationsEnabled: Bool
    var blacklisted: Bool
}
