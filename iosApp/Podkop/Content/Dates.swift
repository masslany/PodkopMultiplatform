import Foundation

enum Dates {
    /// Shared dates are ISO-8601 local date-times without a zone, in the device time zone.
    static func parse(_ raw: String) -> Date? {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = .current
        for format in ["yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm"] {
            formatter.dateFormat = format
            if let date = formatter.date(from: String(raw.prefix(19))) { return date }
        }
        return nil
    }
}
