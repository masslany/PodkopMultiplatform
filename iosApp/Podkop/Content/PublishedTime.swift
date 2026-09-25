import Foundation

/// Android's relative time rules: "chwilę temu", "X min temu", "X godz. Y min temu", "X dni temu",
/// and a full "yyyy.MM.dd HH:mm" date after a week.
enum PublishedTime {
    static func text(_ date: Date, now: Date = Date()) -> String {
        let age = max(0, now.timeIntervalSince(date))
        let minutes = Int(age / 60)
        let hours = minutes / 60
        let days = hours / 24
        if days > 7 {
            let formatter = DateFormatter()
            formatter.locale = Locale(identifier: "en_US_POSIX")
            formatter.dateFormat = "yyyy.MM.dd HH:mm"
            return formatter.string(from: date)
        }
        if hours >= 24 { return String(localized: "\(days) days ago") }
        if hours >= 1 {
            let rest = minutes % 60
            return rest > 0 ? String(localized: "\(hours) h \(rest) min ago") : String(localized: "\(hours) h ago")
        }
        if minutes > 0 { return String(localized: "\(minutes) min ago") }
        return String(localized: "just now")
    }

    static func text(iso raw: String?, now: Date = Date()) -> String? {
        guard let raw, let date = Dates.parse(raw) else { return nil }
        return text(date, now: now)
    }
}
