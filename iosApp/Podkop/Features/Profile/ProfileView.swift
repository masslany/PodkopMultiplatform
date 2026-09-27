import SwiftUI

struct ProfileView: View {
    @State private var model: ProfileModel
    let tab: AppTab
    let dependencies: AppDependencies
    private var router: AppRouter { dependencies.router }

    /// Pass nil to show the signed-in viewer's own profile.
    init(username: String?, tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: ProfileModel(username: username, loader: dependencies.profileLoader,
                                                  updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        Group {
            switch model.phase {
            case .loading:
                ProgressView(.commonLoading).frame(maxWidth: .infinity, maxHeight: .infinity)
            case .failed:
                ContentUnavailableView {
                    Label(.profileCouldNotLoadProfile, systemImage: "person.crop.circle.badge.exclamationmark")
                } actions: {
                    Button(.commonRetry) { model.retry() }
                }
            case .loaded:
                if let profile = model.profile { content(profile) }
            }
        }
        .navigationTitle(model.profile?.username ?? model.username ?? String(localized: .commonProfile))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { if let profile = model.profile, model.phase == .loaded { profileToolbar(profile) } }
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.session.revision) { _, _ in model.setSession() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
        .alert(.commonCouldNotCompleteAction,
               isPresented: Binding(get: { model.actionFailed }, set: { if !$0 { model.dismissActionFailure() } })) {
            Button(.commonOk, role: .cancel) {}
        }
        .alert(.profileNoteWasSaved,
               isPresented: Binding(get: { model.noteSaved }, set: { if !$0 { model.dismissNoteSaved() } })) {
            Button(.commonOk, role: .cancel) {}
        }
    }

    private func content(_ profile: Profile) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                header(profile)
                if model.detailsExpanded { details(profile) }
                summary(profile)
                sectionPicker
                rows
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 20)
            .frame(maxWidth: 900)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await model.refresh() }
    }

    /// Banner with the avatar overlapping its lower edge and the rank on the avatar, as on
    /// Android and wykop.pl. Observe, message and block live in the toolbar.
    private func header(_ profile: Profile) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            ZStack(alignment: .bottomLeading) {
                Group {
                    if let banner = profile.bannerURL {
                        RemoteImage(url: banner, maxDimension: 1200) { bannerPlaceholder }
                    } else {
                        bannerPlaceholder
                    }
                }
                .frame(height: 140)
                .frame(maxWidth: .infinity)
                .clipShape(RoundedRectangle(cornerRadius: PodkopTheme.cardRadius, style: .continuous))
                .padding(.bottom, 48)
                .accessibilityHidden(true)
                AvatarView(url: profile.avatarURL, name: profile.username, size: 92, gender: profile.gender)
                    .padding(3)
                    .background(PodkopTheme.background,
                                in: RoundedRectangle(cornerRadius: 92 * 0.22 + 3, style: .continuous))
                    .overlay(alignment: .topTrailing) {
                        if let rank = profile.rankPosition {
                            Text(verbatim: "#\(rank)")
                                .font(.caption.weight(.bold).monospacedDigit())
                                .foregroundStyle(.white)
                                .padding(.horizontal, 7).padding(.vertical, 3)
                                .background(PodkopTheme.hotOrange, in: Capsule())
                                .offset(x: 10, y: -6)
                                .accessibilityLabel(String(localized: .profileRankPosition(rank)))
                        }
                    }
                    .padding(.leading, 16)
            }
            HStack(alignment: .firstTextBaseline) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(profile.username)
                        .font(.title2.bold())
                        .foregroundStyle(authorColor(profile.color))
                    if let joined = joined(profile) {
                        Text(joined).font(.subheadline).foregroundStyle(.secondary)
                    }
                }
                Spacer()
                Button {
                    withAnimation { model.detailsExpanded.toggle() }
                } label: {
                    Label(model.detailsExpanded ? .profileHideDetails : .profileShowDetails,
                          systemImage: model.detailsExpanded ? "chevron.up" : "chevron.down")
                        .labelStyle(.iconOnly)
                        .frame(width: 32, height: 32)
                        .background(PodkopTheme.card, in: Circle())
                }
                .buttonStyle(.plain)
                .foregroundStyle(.secondary)
                .accessibilityIdentifier("profileDetails")
            }
            .padding(.horizontal, 4)
        }
        .padding(.top, 8)
    }

    private var bannerPlaceholder: some View {
        LinearGradient(colors: [PodkopTheme.cardInset, PodkopTheme.separator],
                       startPoint: .topLeading, endPoint: .bottomTrailing)
    }

    @ToolbarContentBuilder private func profileToolbar(_ profile: Profile) -> some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            if profile.canSendPrivateMessage {
                Button { router.navigate(.conversation(profile.username), in: tab) } label: {
                    Image(systemName: "envelope")
                }
                .accessibilityLabel(.profileSendPrivateMessage)
                .accessibilityIdentifier("profileMessage")
            }
            if profile.canBlacklist {
                Button { model.toggle(.blacklist) } label: {
                    Image(systemName: profile.blacklisted ? "lock.fill" : "lock.open")
                }
                .disabled(model.pending.contains(.blacklist))
                .accessibilityLabel(profile.blacklisted ? .profileUnblockUser : .profileBlockUser)
                .accessibilityIdentifier("profileBlacklist")
            }
            if profile.canManageObservation {
                Button { model.toggle(.observe) } label: {
                    Image(systemName: profile.observed ? "eye.fill" : "eye")
                }
                .disabled(model.pending.contains(.observe))
                .accessibilityLabel(profile.observed ? .commonObserving : .commonObserve)
                .accessibilityIdentifier("profileObserve")
            }
        }
    }

    @ViewBuilder private func details(_ profile: Profile) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            if profile.isLoggedIn && !profile.isOwnProfile { noteSection }
            Text(.profileAchievements).font(.headline)
            if model.badgesFailed {
                HStack {
                    Text(.profileCouldNotLoadAchievements).foregroundStyle(.secondary)
                    Spacer()
                    Button(.commonRetry) { model.retryDetails() }
                }
            } else if !model.badgesLoaded {
                ProgressView().frame(maxWidth: .infinity)
            } else if model.badges.isEmpty {
                Text(.profileNoAchievementsShow).foregroundStyle(.secondary)
            } else {
                // Android's FlowRow of badge tiles; tap one for its details.
                FlowLayout(spacing: 8, lineSpacing: 8) {
                    ForEach(model.badges) { AchievementBadge(badge: $0) }
                }
            }
        }
        .podkopCard(padding: 12)
    }

    @ViewBuilder private var noteSection: some View {
        Text(.profileNoteAboutUser).font(.headline)
        if model.note.failed {
            HStack {
                Text(.profileCouldNotLoadNote).foregroundStyle(.secondary)
                Spacer()
                Button(.commonRetry) { model.retryDetails() }
            }
        } else {
            TextField(String(localized: .profileAddNote), text: $model.note.content, axis: .vertical)
                .lineLimit(2...6)
                .textFieldStyle(.roundedBorder)
                .disabled(model.note.loading || model.note.saving)
                .accessibilityIdentifier("profileNote")
            HStack {
                if model.note.saveFailed {
                    Label(.profileCouldNotSaveText, systemImage: "exclamationmark.circle")
                        .font(.caption).foregroundStyle(.red)
                }
                Spacer()
                Button(.commonSave) { model.saveNote() }
                    .buttonStyle(.borderedProminent)
                    .disabled(!model.note.canSave)
                    .accessibilityIdentifier("profileNoteSave")
            }
        }
    }

    /// Summary tiles: the label above the number, the selected one outlined (Android's profile summary).
    private func summary(_ profile: Profile) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(ProfileSummary.allCases, id: \.self) { item in
                    let selected = model.summary == item
                    Button { model.select(summary: item) } label: {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(title(for: item)).font(.caption).foregroundStyle(.secondary)
                            Text(count(item, profile).formatted())
                                .font(.headline.monospacedDigit())
                                .foregroundStyle(.primary)
                        }
                        .frame(minWidth: 72, alignment: .leading)
                        .padding(.horizontal, 12).padding(.vertical, 8)
                        .background(PodkopTheme.card, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .strokeBorder(selected ? Color.primary : .clear, lineWidth: 2))
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(selected ? .isSelected : [])
                    .accessibilityIdentifier("profileSummary-\(item.rawValue)")
                }
            }
        }
    }

    @ViewBuilder private var sectionPicker: some View {
        let sections = model.summary.sections
        if sections.count > 1 {
            Menu {
                ForEach(sections, id: \.self) { section in
                    Button(title(for: section)) { model.select(section: section) }
                }
            } label: {
                DropdownLabel(title: title(for: model.section))
            }
            .accessibilityIdentifier("profileSection")
        }
    }

    @ViewBuilder private var rows: some View {
        let pager = model.pager
        switch pager.phase {
        case .idle, .loading:
            ProgressView(.commonLoading).frame(maxWidth: .infinity, minHeight: 120)
        case .failed:
            // Android shows a failed section as empty; offer an explicit retry instead.
            ContentUnavailableView {
                Label(.commonCouldNotLoadContent, systemImage: "wifi.exclamationmark")
            } actions: {
                Button(.commonRetry) { pager.retry() }
            }
        case .loaded where pager.items.isEmpty:
            ContentUnavailableView(emptyTitle, systemImage: "tray")
        case .loaded:
            LazyVStack(spacing: 10) {
                ForEach(pager.items) { row in
                    rowView(row).onAppear { pager.loadNextIfNeeded(after: row) }
                }
                PagerFooter(pager: pager)
            }
        }
    }

    @ViewBuilder private func rowView(_ row: ProfileRow) -> some View {
        switch row {
        case .resource(let item):
            ResourceListRow(item: item, tab: tab, dependencies: dependencies)
        case .user(let name, let color, let gender, let online, let verified, let avatarURL):
            Button { router.navigate(.user(name), in: tab) } label: {
                HStack {
                    UserIdentityRow(username: name, color: color, gender: gender, avatarURL: avatarURL)
                    if verified { Image(systemName: "checkmark.seal.fill").foregroundStyle(ContentTokens.brand)
                        .accessibilityLabel(.commonVerifiedAuthor) }
                    if online { Circle().fill(.green).frame(width: 7, height: 7).accessibilityLabel(.profileOnline) }
                    Spacer()
                }
                .podkopCard(padding: 10)
            }
            .buttonStyle(.plain)
        case .tag(let name, let pinned):
            Button { router.navigate(.tag(name), in: tab) } label: {
                HStack {
                    Text(verbatim: "#\(name)").font(.body.bold())
                    if pinned { Image(systemName: "pin.fill").accessibilityLabel(.profilePinned) }
                    Spacer()
                }
                .podkopCard(padding: 10)
            }
            .buttonStyle(.plain)
        }
    }

    private var emptyTitle: LocalizedStringResource {
        switch model.section {
        case .followingTags: .profileNoObservedTagsShow
        case .followers, .followingUsers: .profileNoObservedUsersShow
        default: .commonNothingHereYet
        }
    }

    private func joined(_ profile: Profile) -> String? {
        guard let raw = profile.memberSince, let date = Dates.parse(raw) else { return nil }
        return String(localized: .commonJoined(RelativeDateTimeFormatter().localizedString(for: date, relativeTo: Date())))
    }

    private func count(_ item: ProfileSummary, _ profile: Profile) -> Int {
        switch item {
        case .actions: profile.actions
        case .links: profile.links
        case .entries: profile.entries
        case .followers: profile.followers
        case .following: profile.following
        }
    }

    private func title(for item: ProfileSummary) -> String {
        switch item {
        case .actions: String(localized: .profileActions)
        case .links: String(localized: .commonLinks)
        case .entries: String(localized: .profileMicroblog)
        case .followers: String(localized: .profileFollowers)
        case .following: String(localized: .profileFollowing)
        }
    }

    private func title(for section: ProfileSection) -> String {
        switch section {
        case .actions: String(localized: .commonAll)
        case .entriesAdded, .linksAdded: String(localized: .profileAdded)
        case .entriesVoted: String(localized: .profileUpvoted)
        case .entriesCommented, .linksCommented: String(localized: .commonCommented)
        case .linksPublished: String(localized: .profilePublished)
        case .linksUp: String(localized: .commonDug)
        case .linksDown: String(localized: .profileBuried)
        case .linksRelated: String(localized: .profileRelated)
        case .followers: String(localized: .profileFollowers)
        case .followingTags: String(localized: .commonTags)
        case .followingUsers: String(localized: .commonUsers)
        }
    }
}
