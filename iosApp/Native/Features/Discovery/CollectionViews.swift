import SwiftUI

private struct CollectionScroll<Controls: View, Rows: View>: View {
    let refresh: () async -> Void
    @ViewBuilder let controls: Controls
    @ViewBuilder let rows: Rows

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                controls.buttonStyle(.bordered).padding(.top, 8)
                rows
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 20)
            .frame(maxWidth: 900)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await refresh() }
    }
}

struct NativeHitsView: View {
    @State private var model: HitsModel
    @State private var pickingArchive = false
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: HitsModel(loader: dependencies.collectionLoader,
                                               updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        CollectionScroll(refresh: model.refresh) {
            HStack {
                Menu {
                    ForEach(HitsModel.Sort.allCases, id: \.self) { sort in
                        Button(title(for: sort)) { model.select(sort) }
                    }
                } label: {
                    Label(title(for: model.sort), systemImage: "line.3.horizontal.decrease")
                }
                .accessibilityIdentifier("hitsSort")
                Button { pickingArchive = true } label: {
                    Label(archiveTitle, systemImage: "calendar")
                }
                .accessibilityIdentifier("hitsArchive")
            }
        } rows: {
            PagedResourceRows(pager: model.pager, tab: tab, dependencies: dependencies)
        }
        .navigationTitle("Hits")
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
        .sheet(isPresented: $pickingArchive) {
            HitsArchivePicker(selected: model.archive) { archive in
                pickingArchive = false
                model.select(archive: archive)
            }
            .presentationDetents([.medium])
        }
    }

    private var archiveTitle: String {
        guard let archive = model.archive else { return String(localized: "Archive") }
        return "\(Calendar.current.standaloneMonthSymbols[archive.month - 1]) \(String(archive.year))"
    }

    private func title(for sort: HitsModel.Sort) -> String {
        switch sort {
        case .all: String(localized: "All time")
        case .day: String(localized: "Day")
        case .week: String(localized: "Week")
        case .month: String(localized: "Month")
        case .year: String(localized: "Year")
        }
    }
}

private struct HitsArchivePicker: View {
    let confirm: (HitsArchive) -> Void
    @State private var year: Int
    @State private var month: Int
    @Environment(\.dismiss) private var dismiss
    private let maxYear = Calendar.current.component(.year, from: Date())

    init(selected: HitsArchive?, confirm: @escaping (HitsArchive) -> Void) {
        let now = Calendar.current.dateComponents([.year, .month], from: Date())
        _year = State(initialValue: selected?.year ?? now.year ?? HitsArchive.startYear)
        _month = State(initialValue: selected?.month ?? now.month ?? 1)
        self.confirm = confirm
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                HStack {
                    Button { changeYear(-1) } label: { Image(systemName: "chevron.left") }
                        .disabled(year <= HitsArchive.startYear)
                        .accessibilityLabel("Previous year")
                    Text(String(year)).font(.title2.monospacedDigit()).frame(minWidth: 80)
                    Button { changeYear(1) } label: { Image(systemName: "chevron.right") }
                        .disabled(year >= maxYear)
                        .accessibilityLabel("Next year")
                }
                LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 4), spacing: 10) {
                    ForEach(1...12, id: \.self) { value in
                        let enabled = HitsArchive.isAvailable(year: year, month: value)
                        Button(Calendar.current.shortStandaloneMonthSymbols[value - 1]) { month = value }
                            .buttonStyle(.bordered)
                            .tint(value == month ? ContentTokens.brand : .secondary)
                            .disabled(!enabled)
                    }
                }
                Spacer()
            }
            .padding()
            .navigationTitle("Archive")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Show") { confirm(HitsArchive(year: year, month: month)) }
                        .disabled(!HitsArchive.isAvailable(year: year, month: month))
                }
            }
        }
    }

    /// Keeps the month when possible, otherwise selects the first available month of the year.
    private func changeYear(_ delta: Int) {
        year = min(max(year + delta, HitsArchive.startYear), maxYear)
        if !HitsArchive.isAvailable(year: year, month: month) {
            month = (1...12).first { HitsArchive.isAvailable(year: year, month: $0) } ?? 1
        }
    }
}

struct NativeRankView: View {
    @State private var model: RankModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: RankModel(loader: dependencies.collectionLoader))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        List {
            switch model.pager.phase {
            case .idle, .loading:
                ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 180)
            case .failed:
                ContentUnavailableView {
                    Label("Could not load content", systemImage: "wifi.exclamationmark")
                } actions: {
                    Button("Retry") { model.pager.retry() }
                }
            case .loaded:
                if model.pager.refreshError {
                    Label("Could not refresh", systemImage: "exclamationmark.triangle")
                }
                ForEach(model.pager.items) { user in
                    Button { dependencies.router.navigate(.user(user.username), in: tab) } label: {
                        RankRow(user: user)
                    }
                    .foregroundStyle(.primary)
                    .onAppear { model.pager.loadNextIfNeeded(after: user) }
                }
                PagerFooter(pager: model.pager)
            }
        }
        .refreshable { await model.refresh() }
        .navigationTitle("Rank")
        .task { model.start() }
        .onDisappear { model.stop() }
    }
}

private struct RankRow: View {
    let user: NativeRankUser

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(spacing: 2) {
                Text("#\(user.position)").font(.headline.monospacedDigit())
                if user.trend != 0 {
                    Label("\(abs(user.trend))", systemImage: user.trend > 0 ? "arrow.up" : "arrow.down")
                        .font(.caption2)
                        .foregroundStyle(user.trend > 0 ? .green : .red)
                        .accessibilityLabel(user.trend > 0
                            ? String(localized: "Up \(abs(user.trend))")
                            : String(localized: "Down \(abs(user.trend))"))
                }
            }
            .frame(minWidth: 44)
            VStack(alignment: .leading, spacing: 6) {
                UserIdentityRow(username: user.username, color: user.color, gender: user.gender,
                                detail: memberSince, avatarURL: user.avatarURL)
                ViewThatFits {
                    HStack(spacing: 12) { counts }
                    VStack(alignment: .leading, spacing: 2) { counts }
                }
                .font(.caption)
                .foregroundStyle(.secondary)
            }
        }
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder private var counts: some View {
        Text("Actions: \(user.actions)")
        Text("Links: \(user.links)")
        Text("Entries: \(user.entries)")
        Text("Followers: \(user.followers)")
    }

    private var memberSince: String? {
        guard let raw = user.memberSince, let date = NativeDates.parse(raw) else { return nil }
        let text = RelativeDateTimeFormatter().localizedString(for: date, relativeTo: Date())
        return String(localized: "Joined \(text)")
    }
}

enum NativeDates {
    /// Shared dates are ISO-8601 local date-times without a zone, in the device time zone.
    static func parse(_ raw: String) -> Date? {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = .current
        for format in ["yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm"] {
            formatter.dateFormat = format
            if let date = formatter.date(from: String(raw.prefix(19))) { return date }
        }
        return nil
    }
}

struct NativeFavouritesView: View {
    @State private var model: FavouritesModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: FavouritesModel(loader: dependencies.collectionLoader,
                                                     isLoggedIn: dependencies.session.isLoggedIn,
                                                     updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        CollectionScroll(refresh: model.refresh) {
            HStack {
                Menu {
                    ForEach(FavouritesModel.Kind.allCases, id: \.self) { kind in
                        Button(title(for: kind)) { model.select(kind: kind) }
                    }
                } label: {
                    Label(title(for: model.kind), systemImage: "square.stack")
                }
                .accessibilityIdentifier("favouritesType")
                Menu {
                    Button("Newest") { model.select(sort: .newest) }
                    Button("Oldest") { model.select(sort: .oldest) }
                } label: {
                    Label(model.sort == .newest ? String(localized: "Newest") : String(localized: "Oldest"),
                          systemImage: "arrow.up.arrow.down")
                }
                .accessibilityIdentifier("favouritesSort")
            }
        } rows: {
            PagedResourceRows(pager: model.pager, tab: tab, dependencies: dependencies,
                              emptyTitle: emptyTitle)
        }
        .navigationTitle("Favorites")
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.session.revision) { _, _ in
            model.setSession(dependencies.session.isLoggedIn)
        }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
    }

    private var emptyTitle: LocalizedStringKey {
        switch model.kind {
        case .all: "No favorites yet"
        case .link: "No favorite links"
        case .entry: "No favorite entries"
        case .linkComment: "No favorite link comments"
        case .entryComment: "No favorite entry comments"
        }
    }

    private func title(for kind: FavouritesModel.Kind) -> String {
        switch kind {
        case .all: String(localized: "Everything")
        case .link: String(localized: "Links")
        case .entry: String(localized: "Entries")
        case .linkComment: String(localized: "Link comments")
        case .entryComment: String(localized: "Entry comments")
        }
    }
}

struct NativeObservedView: View {
    @Environment(\.openURL) private var openURL
    @State private var model: ObservedModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: ObservedModel(loader: dependencies.collectionLoader,
                                                   updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        CollectionScroll(refresh: model.refresh) {
            Picker("Type", selection: Binding(get: { model.kind }, set: { model.select($0) })) {
                ForEach(ObservedModel.Kind.allCases, id: \.self) { Text(title(for: $0)).tag($0) }
            }
            .pickerStyle(.segmented)
            .accessibilityIdentifier("observedType")
        } rows: {
            rows
        }
        .navigationTitle("Observed")
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
    }

    @ViewBuilder private var rows: some View {
        let pager = model.pager
        switch pager.phase {
        case .idle, .loading:
            ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 180)
        case .failed:
            ContentUnavailableView {
                Label("Could not load content", systemImage: "wifi.exclamationmark")
            } actions: {
                Button("Retry") { pager.retry() }
            }
        case .loaded where pager.items.isEmpty:
            ContentUnavailableView(emptyTitle, systemImage: "tray")
        case .loaded:
            LazyVStack(spacing: 12) {
                ForEach(pager.items) { item in
                    VStack(alignment: .leading, spacing: 6) {
                        if let count = item.newContentCount, count > 0,
                           item.resource.kind == .link || item.resource.kind == .entry {
                            Label(item.resource.kind == .link
                                  ? String(localized: "\(count) new comments on an observed link")
                                  : String(localized: "\(count) new comments on an observed entry"),
                                  systemImage: "bell.badge")
                                .font(.caption.bold())
                                .foregroundStyle(ContentTokens.brand)
                        }
                        NativeResourceCard(
                            resource: item.resource,
                            actions: .navigation(for: item.resource, in: tab, dependencies: dependencies, openURL: openURL),
                            autoplayGifs: dependencies.session.autoplayGifs,
                            isForeground: dependencies.isForeground
                        )
                    }
                    .onAppear { pager.loadNextIfNeeded(after: item) }
                }
                PagerFooter(pager: pager)
            }
        }
    }

    private var emptyTitle: LocalizedStringKey {
        switch model.kind {
        case .all: "Nothing observed yet"
        case .profiles: "No content from observed profiles"
        case .discussions: "No observed discussions"
        case .tags: "No content from observed tags"
        }
    }

    private func title(for kind: ObservedModel.Kind) -> String {
        switch kind {
        case .all: String(localized: "All")
        case .profiles: String(localized: "Profiles")
        case .discussions: String(localized: "Discussions")
        case .tags: String(localized: "Tags")
        }
    }
}
