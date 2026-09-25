import Observation

@MainActor @Observable
final class VoterModel {
    private(set) var voters: [NativeVoter] = []
    private(set) var loading = false
    private(set) var failed = false
    private(set) var exhausted = false
    private var page = 1
    private var task: Task<Void, Never>?
    let target: VoterTarget
    private let loader: VoterLoading

    init(target: VoterTarget, loader: VoterLoading) {
        self.target = target
        self.loader = loader
    }

    func load() {
        guard !loading, !exhausted else { return }
        loading = true
        failed = false
        let requestedPage = page
        task = Task { [weak self] in
            guard let self else { return }
            do {
                let value = try await loader.load(target: target, page: requestedPage)
                guard !Task.isCancelled else { return }
                let fresh = value.items.filter { item in !voters.contains { $0.username == item.username } }
                voters.append(contentsOf: fresh)
                page += 1
                exhausted = value.items.isEmpty || fresh.isEmpty ||
                    value.total.map { voters.count >= $0 } == true
            } catch is CancellationError {
                return
            } catch {
                failed = true
            }
            loading = false
            task = nil
        }
    }

    func stop() { task?.cancel(); task = nil; loading = false }
}
