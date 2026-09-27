import Foundation
import Observation

@MainActor @Observable
final class SceneActivity {
    private var activeScenes = Set<UUID>()
    var isForeground: Bool { !activeScenes.isEmpty }
    var activeCount: Int { activeScenes.count }

    func set(_ id: UUID, active: Bool) {
        if active { activeScenes.insert(id) } else { activeScenes.remove(id) }
    }
}
