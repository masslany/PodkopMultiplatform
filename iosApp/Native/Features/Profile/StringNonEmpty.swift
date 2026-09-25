import Foundation
import Observation
import PodkopShared

extension String {
    /// Treats blank server strings as absent.
    var nonEmpty: String? { trimmingCharacters(in: .whitespaces).isEmpty ? nil : self }
}
