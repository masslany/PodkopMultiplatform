import Foundation
import Observation

@MainActor @Observable
final class InboxModel {
    let pager = ListPager<Conversation>(key: \.id)
    private let loader: MessagesLoading

    init(loader: MessagesLoading) { self.loader = loader }

    func start() {
        guard pager.phase == .idle else { return }
        let loader = loader
        pager.load(first: loader.firstRequest()) { try await loader.conversations(request: $0, loaded: $1) }
    }

    func refresh() async { await pager.refresh() }
    func stop() { pager.stop() }
}

/// Username entry with debounced suggestions; a conversation opens from a suggestion, as on Android.
