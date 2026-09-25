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
                ProgressView("Loading…").frame(maxWidth: .infinity, maxHeight: .infinity)
            case .failed:
                ContentUnavailableView {
                    Label("Could not load the profile.", systemImage: "person.crop.circle.badge.exclamationmark")
                } actions: {
                    Button("Retry") { model.retry() }
                }
            case .loaded:
                if let profile = model.profile { content(profile) }
            }
        }
        .navigationTitle(model.profile?.username ?? model.username ?? String(localized: "Profile"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { if let profile = model.profile, model.phase == .loaded { profileToolbar(profile) } }
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.session.revision) { _, _ in model.setSession() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
        .alert("Could not complete this action. Try again.",
               isPresented: Binding(get: { model.actionFailed }, set: { if !$0 { model.dismissActionFailure() } })) {
            Button("OK", role: .cancel) {}
        }
        .alert("The note was saved.",
               isPresented: Binding(get: { model.noteSaved }, set: { if !$0 { model.dismissNoteSaved() } })) {
            Button("OK", role: .cancel) {}
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
                .clipShape(RoundedRectangle(cornerRadius: WykopTheme.cardRadius, style: .continuous))
                .padding(.bottom, 48)
                .accessibilityHidden(true)
                AvatarView(url: profile.avatarURL, name: profile.username, size: 92, gender: profile.gender)
                    .padding(3)
                    .background(WykopTheme.background,
                                in: RoundedRectangle(cornerRadius: 92 * 0.22 + 3, style: .continuous))
                    .overlay(alignment: .topTrailing) {
                        if let rank = profile.rankPosition {
                            Text(verbatim: "#\(rank)")
                                .font(.caption.weight(.bold).monospacedDigit())
                                .foregroundStyle(.white)
                                .padding(.horizontal, 7).padding(.vertical, 3)
                                .background(WykopTheme.tagBlue, in: Capsule())
                                .offset(x: 10, y: -6)
                                .accessibilityLabel(String(localized: "Rank position \(rank)"))
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
                    Label(model.detailsExpanded ? "Hide details" : "Show details",
                          systemImage: model.detailsExpanded ? "chevron.up" : "chevron.down")
                        .labelStyle(.iconOnly)
                        .frame(width: 32, height: 32)
                        .background(WykopTheme.card, in: Circle())
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
        LinearGradient(colors: [WykopTheme.cardInset, WykopTheme.separator],
                       startPoint: .topLeading, endPoint: .bottomTrailing)
    }

    @ToolbarContentBuilder private func profileToolbar(_ profile: Profile) -> some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            if profile.canSendPrivateMessage {
                Button { router.navigate(.conversation(profile.username), in: tab) } label: {
                    Image(systemName: "envelope")
                }
                .accessibilityLabel("Send a private message")
                .accessibilityIdentifier("profileMessage")
            }
            if profile.canBlacklist {
                Button { model.toggle(.blacklist) } label: {
                    Image(systemName: profile.blacklisted ? "lock.fill" : "lock.open")
                }
                .disabled(model.pending.contains(.blacklist))
                .accessibilityLabel(profile.blacklisted ? "Unblock user" : "Block user")
                .accessibilityIdentifier("profileBlacklist")
            }
            if profile.canManageObservation {
                Button { model.toggle(.observe) } label: {
                    Image(systemName: profile.observed ? "eye.fill" : "eye")
                }
                .disabled(model.pending.contains(.observe))
                .accessibilityLabel(profile.observed ? "Observing" : "Observe")
                .accessibilityIdentifier("profileObserve")
            }
        }
    }

    @ViewBuilder private func details(_ profile: Profile) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            if profile.isLoggedIn && !profile.isOwnProfile { noteSection }
            Text("Achievements").font(.headline)
            if model.badgesFailed {
                HStack {
                    Text("Could not load achievements.").foregroundStyle(.secondary)
                    Spacer()
                    Button("Retry") { model.retryDetails() }
                }
            } else if !model.badgesLoaded {
                ProgressView().frame(maxWidth: .infinity)
            } else if model.badges.isEmpty {
                Text("No achievements to show.").foregroundStyle(.secondary)
            } else {
                ForEach(model.badges) { badge in
                    VStack(alignment: .leading, spacing: 2) {
                        Text(badge.label).font(.subheadline.bold())
                            .foregroundStyle(Color(hex: badge.colorHex) ?? .primary)
                        if !badge.description.isEmpty {
                            Text(badge.description).font(.caption)
                        }
                        HStack(spacing: 8) {
                            if let level = badge.level { Text("Level \(level)") }
                            if let progress = badge.progress { Text("Progress \(progress)%") }
                            if let date = badge.achievedAt.flatMap(Dates.parse) {
                                Text("Achieved \(date.formatted(date: .numeric, time: .omitted))")
                            }
                        }
                        .font(.caption2).foregroundStyle(.secondary)
                    }
                    .accessibilityElement(children: .combine)
                }
            }
        }
        .wykopCard(padding: 12)
    }

    @ViewBuilder private var noteSection: some View {
        Text("Note about this user").font(.headline)
        if model.note.failed {
            HStack {
                Text("Could not load the note.").foregroundStyle(.secondary)
                Spacer()
                Button("Retry") { model.retryDetails() }
            }
        } else {
            TextField("Add a note", text: $model.note.content, axis: .vertical)
                .lineLimit(2...6)
                .textFieldStyle(.roundedBorder)
                .disabled(model.note.loading || model.note.saving)
                .accessibilityIdentifier("profileNote")
            HStack {
                if model.note.saveFailed {
                    Label("Could not save. Your text is kept.", systemImage: "exclamationmark.circle")
                        .font(.caption).foregroundStyle(.red)
                }
                Spacer()
                Button("Save") { model.saveNote() }
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
                        .background(WykopTheme.card, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
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
            ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 120)
        case .failed:
            // Android shows a failed section as empty; offer an explicit retry instead.
            ContentUnavailableView {
                Label("Could not load content", systemImage: "wifi.exclamationmark")
            } actions: {
                Button("Retry") { pager.retry() }
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
                        .accessibilityLabel("Verified author") }
                    if online { Circle().fill(.green).frame(width: 7, height: 7).accessibilityLabel("Online") }
                    Spacer()
                }
                .wykopCard(padding: 10)
            }
            .buttonStyle(.plain)
        case .tag(let name, let pinned):
            Button { router.navigate(.tag(name), in: tab) } label: {
                HStack {
                    Text("#\(name)").font(.body.bold())
                    if pinned { Image(systemName: "pin.fill").accessibilityLabel("Pinned") }
                    Spacer()
                }
                .wykopCard(padding: 10)
            }
            .buttonStyle(.plain)
        }
    }

    private var emptyTitle: LocalizedStringKey {
        switch model.section {
        case .followingTags: "No observed tags to show."
        case .followers, .followingUsers: "No observed users to show."
        default: "Nothing here yet"
        }
    }

    private func joined(_ profile: Profile) -> String? {
        guard let raw = profile.memberSince, let date = Dates.parse(raw) else { return nil }
        return String(localized: "Joined \(RelativeDateTimeFormatter().localizedString(for: date, relativeTo: Date()))")
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
        case .actions: String(localized: "Actions")
        case .links: String(localized: "Links")
        case .entries: String(localized: "Microblog")
        case .followers: String(localized: "Followers")
        case .following: String(localized: "Following")
        }
    }

    private func title(for section: ProfileSection) -> String {
        switch section {
        case .actions: String(localized: "All")
        case .entriesAdded, .linksAdded: String(localized: "Added")
        case .entriesVoted: String(localized: "Upvoted")
        case .entriesCommented, .linksCommented: String(localized: "Commented")
        case .linksPublished: String(localized: "Published")
        case .linksUp: String(localized: "Dug")
        case .linksDown: String(localized: "Buried")
        case .linksRelated: String(localized: "Related")
        case .followers: String(localized: "Followers")
        case .followingTags: String(localized: "Tags")
        case .followingUsers: String(localized: "Users")
        }
    }
}
