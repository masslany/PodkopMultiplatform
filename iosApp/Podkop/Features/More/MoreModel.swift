import Foundation
import Observation

/// The signed-in user's profile preview for the More tab (Android's `MoreViewModel` header).
/// Sections and badges come straight from the session, so only the profile is loaded here.
@MainActor @Observable
final class MoreModel {
    private(set) var profile: Profile?
    private(set) var loading = false
    private let loader: ProfileLoading
    private var loadedRevision: Int?
    private var task: Task<Void, Never>?

    init(loader: ProfileLoading) {
        self.loader = loader
    }

    /// Loads the preview once per session; signing out clears it immediately.
    func update(loggedIn: Bool, revision: Int) {
        guard loggedIn else {
            task?.cancel()
            profile = nil
            loading = false
            loadedRevision = nil
            return
        }
        guard loadedRevision != revision else { return }
        // Another account's preview must never stay on screen.
        if loadedRevision != nil { profile = nil }
        loadedRevision = revision
        reload()
    }

    /// Refreshes the preview when the tab reappears, keeping the current one on screen meanwhile.
    func refresh() {
        guard loadedRevision != nil, !loading else { return }
        reload()
    }

    private func reload() {
        task?.cancel()
        loading = true
        task = Task { [weak self, loader] in
            do {
                let username = try await loader.ownUsername()
                let value = try await loader.profile(username)
                guard !Task.isCancelled else { return }
                self?.profile = value
            } catch {
                // Like Android, a failed preview only hides the header; the menu still works.
            }
            if !Task.isCancelled { self?.loading = false }
        }
    }
}
