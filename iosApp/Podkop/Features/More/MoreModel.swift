import Foundation
import Observation

/// The signed-in user's profile preview for the More tab (Android's `MoreViewModel` header).
/// Sections and badges come straight from the session, so only the profile is loaded here.
@MainActor @Observable
final class MoreModel {
    private(set) var profile: Profile?
    private(set) var loading = false
    /// Changes after a pull to refresh, so the header's images load again.
    private(set) var imageRevision = 0
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

    /// Pull to refresh: reloads the profile and downloads the avatar and banner again, which
    /// otherwise stay cached on disk.
    func forceRefresh(media: MediaLoading?) async {
        guard loadedRevision != nil else { return }
        task?.cancel()
        loading = true
        var urls = profile.map(Self.imageURLs) ?? []
        if let username = try? await loader.ownUsername(), let value = try? await loader.profile(username),
           !Task.isCancelled {
            urls += Self.imageURLs(value)
            await media?.evict(urls)
            profile = value
        } else {
            await media?.evict(urls)
        }
        imageRevision += 1
        loading = false
    }

    private static func imageURLs(_ profile: Profile) -> [String] {
        [profile.avatarURL, profile.bannerURL].compactMap { $0 }
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
