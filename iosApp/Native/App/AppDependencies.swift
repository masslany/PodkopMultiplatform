import Foundation
import Observation
import PodkopShared

@MainActor
final class AppDependencies {
    static let shared = AppDependencies()
    let client = PodkopClient.companion.create()
    let adapter = BridgeAdapter()
    let router = AppRouter()
    lazy var ingress = LinkIngress(router: router)
    lazy var session = SessionModel(dependencies: self)
    let sceneActivity = SceneActivity()
    let resourceUpdates = ResourceUpdates()
    lazy var feedLoader: FeedLoading = {
        #if DEBUG
        if isFixture { return FixtureFeedLoader() }
        #endif
        return SharedFeedLoader(client: client, adapter: adapter)
    }()
    lazy var detailLoader: DetailLoading = {
        #if DEBUG
        if isFixture { return FixtureDetailLoader() }
        #endif
        return SharedDetailLoader(client: client, adapter: adapter)
    }()
    lazy var detailMutator: DetailMutating = {
        #if DEBUG
        if isFixture { return FixtureDetailMutator() }
        #endif
        return SharedDetailMutator(client: client, adapter: adapter)
    }()
    lazy var interactor = ResourceInteractor(mutator: detailMutator, updates: resourceUpdates) { [router] in
        router.banner = String(localized: "Could not complete this action. Try again.")
    }
    lazy var voterLoader: VoterLoading = {
        #if DEBUG
        if isFixture { return FixtureVoterLoader() }
        #endif
        return SharedVoterLoader(client: client, adapter: adapter)
    }()
    lazy var composerSubmitter: ComposerSubmitting = {
        #if DEBUG
        if isFixture { return FixtureComposerSubmitter() }
        #endif
        return SharedComposerSubmitter(client: client, adapter: adapter)
    }()
    lazy var composerMedia: ComposerMediaHandling = {
        #if DEBUG
        if isFixture { return FixtureComposerMedia() }
        #endif
        return SharedComposerMedia(client: client, adapter: adapter)
    }()
    lazy var linkDrafting: LinkDrafting = {
        #if DEBUG
        if isFixture { return FixtureLinkDrafting() }
        #endif
        return SharedLinkDrafting(client: client, adapter: adapter)
    }()
    lazy var linkDraftMedia: ComposerMediaHandling = {
        #if DEBUG
        if isFixture { return FixtureComposerMedia() }
        #endif
        return SharedComposerMedia(client: client, adapter: adapter, forLink: true)
    }()
    lazy var searchSuggesting: SearchSuggesting = {
        #if DEBUG
        if isFixture { return FixtureSearchSuggesting() }
        #endif
        return SharedSearchSuggesting(client: client, adapter: adapter)
    }()
    lazy var advancedSearching: AdvancedSearching = {
        #if DEBUG
        if isFixture { return FixtureAdvancedSearching() }
        #endif
        return SharedAdvancedSearching(client: client, adapter: adapter)
    }()
    lazy var collectionLoader: CollectionLoading = {
        #if DEBUG
        if isFixture { return FixtureCollectionLoader() }
        #endif
        return SharedCollectionLoader(client: client, adapter: adapter)
    }()
    lazy var tagLoader: TagLoading = {
        #if DEBUG
        if isFixture { return FixtureTagLoader() }
        #endif
        return SharedTagLoader(client: client, adapter: adapter)
    }()
    lazy var profileLoader: ProfileLoading = {
        #if DEBUG
        if isFixture { return FixtureProfileLoader() }
        #endif
        return SharedProfileLoader(client: client, adapter: adapter)
    }()
    lazy var blacklistsLoader: BlacklistsLoading = {
        #if DEBUG
        if isFixture { return FixtureBlacklistsLoader() }
        #endif
        return SharedBlacklistsLoader(client: client, adapter: adapter)
    }()
    lazy var notificationsLoader: NotificationsLoading = {
        #if DEBUG
        if isFixture { return FixtureNotificationsLoader() }
        #endif
        return SharedNotificationsLoader(client: client, adapter: adapter)
    }()
    lazy var messagesLoader: MessagesLoading = {
        #if DEBUG
        if isFixture { return FixtureMessagesLoader() }
        #endif
        return SharedMessagesLoader(client: client, adapter: adapter)
    }()
    lazy var mediaLoader: MediaLoading = {
        #if DEBUG
        if isFixture { return FixtureMediaLoader() }
        #endif
        return SharedMediaLoader(client: client, adapter: adapter)
    }()
    lazy var settingsService: SettingsServicing = {
        #if DEBUG
        if isFixture { return FixtureSettingsService(session: session) }
        #endif
        return SharedSettingsService(client: client, adapter: adapter, media: mediaLoader as? SharedMediaLoader)
    }()
    private var isFixture: Bool {
        #if DEBUG
        ProcessInfo.processInfo.arguments.contains("-nativeFixture")
        #else
        false
        #endif
    }
    var isForeground: Bool { sceneActivity.isForeground }

    private init() {}

    func scene(_ id: UUID, active: Bool) {
        sceneActivity.set(id, active: active)
        updatePolling()
    }

    func updatePolling() {
        if !isFixture && isForeground && session.phase == .ready {
            client.notifications.startPolling()
        } else {
            client.notifications.stopPolling()
        }
    }

    func close() {
        client.notifications.stopPolling()
        adapter.close()
        client.close()
    }

    func loadTweet(_ url: String) async throws -> NativeTweetPreview {
        let value: IOSTweetPreview = try await adapter.call {
            self.client.embeds.twitterPreview(url: url, completion: $0)
        }
        return NativeTweetPreview(value)
    }
}
