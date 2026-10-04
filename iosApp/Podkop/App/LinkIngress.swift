import Foundation
import Observation

@MainActor
final class LinkIngress {
    private let router: AppRouter
    private var pending: [AppRoute] = []
    private var activeURLs = Set<String>()
    private var recentURLs: [String: Date] = [:]
    var ready = false { didSet { if ready { flush() } } }

    init(router: AppRouter) { self.router = router }

    func begin(_ url: URL, now: Date = Date()) -> Bool {
        recentURLs = recentURLs.filter { now.timeIntervalSince($0.value) < 3 }
        let key = url.absoluteString
        guard !activeURLs.contains(key), recentURLs[key] == nil else { return false }
        activeURLs.insert(key)
        recentURLs[key] = now
        return true
    }
    func end(_ url: URL) { activeURLs.remove(url.absoluteString) }

    func accept(kind: String, id: Int?) {
        let route: AppRoute?
        switch kind {
        case "link": route = id.map(AppRoute.link)
        case "entry": route = id.map(AppRoute.entry)
        case "messages": route = .messages
        case "hits": route = .hits
        case "login": route = nil
        default: route = nil
        }
        guard let route else { return }
        if ready { router.navigate(route) } else { pending.append(route) }
    }

    func flush() {
        guard ready else { return }
        pending.forEach { router.navigate($0) }
        pending.removeAll()
    }
}
