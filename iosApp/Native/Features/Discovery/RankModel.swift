import Foundation
import Observation

@MainActor @Observable
final class RankModel {
    let pager = ListPager<NativeRankUser>(key: \.username)
    private let loader: CollectionLoading

    init(loader: CollectionLoading) { self.loader = loader }

    func start() {
        guard pager.phase == .idle else { return }
        let loader = loader
        pager.load(first: loader.rankFirstRequest()) { try await loader.rank(request: $0, loaded: $1) }
    }

    func refresh() async { await pager.refresh() }
    func stop() { pager.stop() }
}
