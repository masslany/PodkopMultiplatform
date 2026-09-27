import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureAdvancedSearching: AdvancedSearching {
    func build(_ form: AdvancedSearchForm) -> Result<AdvancedSearchRequest, AdvancedSearchValidation> {
        form.query.trimmingCharacters(in: .whitespaces).isEmpty
            ? .failure(.queryRequired) : .success(AdvancedSearchRequest(value: nil))
    }
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func load(_ query: AdvancedSearchRequest, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> {
        ListPage(items: [ContentFixtures.link], next: nil, total: 1)
    }
}
#endif
