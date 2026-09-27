import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class BlacklistsModel {
    var selected: BlacklistCategory = .users
    let categories: [BlacklistCategory: BlacklistCategoryModel]

    init(loader: BlacklistsLoading, suggester: SearchSuggesting) {
        categories = Dictionary(uniqueKeysWithValues: BlacklistCategory.allCases.map {
            ($0, BlacklistCategoryModel(category: $0, loader: loader, suggester: suggester))
        })
    }

    var current: BlacklistCategoryModel { categories[selected]! }

    func start() { categories.values.forEach { $0.start() } }

    func refresh() async {
        await withTaskGroup(of: Void.self) { group in
            for model in categories.values { group.addTask { await model.refresh() } }
        }
    }

    func stop() { categories.values.forEach { $0.stop() } }
}
