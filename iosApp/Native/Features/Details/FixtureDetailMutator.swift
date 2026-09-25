import Foundation

#if DEBUG
@MainActor
final class FixtureDetailMutator: DetailMutating {
    func apply(_ mutation: DetailMutation) async throws {}
}
#endif
