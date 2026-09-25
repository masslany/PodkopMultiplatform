import Foundation
import Observation

@MainActor
func reconciled(_ page: ListPage<Resource>, _ updates: ResourceUpdates?,
                keepFavouritesOnly: Bool = false) -> ListPage<Resource> {
    guard let updates else { return page }
    let items = page.items.compactMap { original in
        updates.reconcile(original).flatMap {
            keepFavouritesOnly && unfavourited(original, $0) ? nil : $0
        }
    }
    return ListPage(items: items, next: page.next, total: page.total)
}

/// Only a confirmed change removes a favourite; server rows are trusted as listed.
func unfavourited(_ original: Resource, _ current: Resource) -> Bool {
    original.favourite && !current.favourite
}
