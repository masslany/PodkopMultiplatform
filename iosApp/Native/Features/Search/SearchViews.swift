import SwiftUI

struct NativeSearchView: View {
    @State private var model: NativeSearchModel
    let tab: AppTab
    let dependencies: AppDependencies
    private var router: AppRouter { dependencies.router }
    private var session: SessionModel { dependencies.session }

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: NativeSearchModel(service: dependencies.searchSuggesting,
                                                       isLoggedIn: dependencies.session.isLoggedIn))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        List {
            Section {
                Button {
                    router.navigate(.advancedSearch(model.normalizedQuery), in: tab)
                } label: {
                    Label("Advanced search", systemImage: "slider.horizontal.3")
                }
                .accessibilityIdentifier("advancedSearch")
            }
            if model.normalizedQuery.isEmpty {
                Section { Text("Search for tags and users.").foregroundStyle(.secondary) }
            } else if model.normalizedQuery.count < model.minimumQueryLength {
                Section {
                    Text("Type at least \(model.minimumQueryLength) characters.").foregroundStyle(.secondary)
                }
            } else {
                tagSection
                if model.isLoggedIn { userSection }
            }
        }
        .searchable(text: $model.query, placement: .navigationBarDrawer(displayMode: .always),
                    prompt: Text("Search tags and users"))
        .autocorrectionDisabled()
        .textInputAutocapitalization(.never)
        .navigationTitle("Search")
        .onChange(of: session.isLoggedIn) { _, value in model.setSession(value) }
        .onAppear { model.resume() }
        .onDisappear { model.stop() }
    }

    private var tagSection: some View {
        Section("Tags") {
            switch model.tagsStatus {
            case .idle, .loading: ProgressView().frame(maxWidth: .infinity)
            case .failed: retryRow("Could not load tags.") { model.retryTags() }
            case .loaded where model.tags.isEmpty: Text("No results").foregroundStyle(.secondary)
            case .loaded:
                ForEach(model.tags) { tag in
                    Button { router.navigate(.tag(tag.name), in: tab) } label: {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("#\(tag.name)").font(.body.bold())
                            Text("Followers: \(tag.followers)").font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .foregroundStyle(.primary)
                }
            }
        }
    }

    private var userSection: some View {
        Section("Users") {
            switch model.usersStatus {
            case .idle, .loading: ProgressView().frame(maxWidth: .infinity)
            case .failed: retryRow("Could not load users.") { model.retryUsers() }
            case .loaded where model.users.isEmpty: Text("No results").foregroundStyle(.secondary)
            case .loaded:
                ForEach(model.users) { user in
                    Button { router.navigate(.user(user.username), in: tab) } label: {
                        UserIdentityRow(username: user.username, color: user.color, gender: user.gender,
                                        avatarURL: user.avatarURL)
                    }
                }
            }
        }
    }

    private func retryRow(_ message: LocalizedStringKey, retry: @escaping () -> Void) -> some View {
        HStack {
            Text(message).foregroundStyle(.secondary)
            Spacer()
            Button("Retry", action: retry)
        }
    }
}

struct NativeAdvancedSearchView: View {
    @State private var model: AdvancedSearchModel
    @State private var filtersExpanded = true
    let tab: AppTab
    let dependencies: AppDependencies

    init(initialQuery: String, tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: AdvancedSearchModel(initialQuery: initialQuery,
                                                         service: dependencies.advancedSearching,
                                                         updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                DisclosureGroup(isExpanded: $filtersExpanded) { filters } label: {
                    Text("Filters").font(.headline)
                }
                if model.hasSearched {
                    if let total = model.results.total, model.results.phase == .loaded {
                        Text("Results: \(total)").font(.subheadline).foregroundStyle(.secondary)
                    }
                    PagedResourceRows(pager: model.results, tab: tab, dependencies: dependencies,
                                      emptyTitle: "No results")
                } else {
                    ContentUnavailableView("Set filters and search.", systemImage: "magnifyingglass")
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 20)
            .frame(maxWidth: 900)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await model.refresh() }
        .navigationTitle("Advanced search")
        .task { model.resume() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
    }

    @ViewBuilder private var filters: some View {
        VStack(alignment: .leading, spacing: 14) {
            TextField("Search phrase", text: $model.form.query)
                .textFieldStyle(.roundedBorder)
                .submitLabel(.search)
                .onSubmit(search)
                .accessibilityIdentifier("advancedQuery")
            LabeledContent("Sort") {
                Picker("Sort", selection: $model.form.sort) {
                    ForEach(AdvancedSearchForm.Sort.allCases, id: \.self) { Text(title(for: $0)).tag($0) }
                }
                .labelsHidden()
            }
            LabeledContent("Minimum votes") {
                Picker("Minimum votes", selection: $model.form.minimumVotes) {
                    ForEach(AdvancedSearchForm.voteOptions, id: \.self) { value in
                        Text(value.map { "\($0)+" } ?? String(localized: "All")).tag(value)
                    }
                }
                .labelsHidden()
            }
            LabeledContent("Date") {
                Picker("Date", selection: $model.form.datePreset) {
                    ForEach(AdvancedSearchForm.DatePreset.allCases, id: \.self) { Text(title(for: $0)).tag($0) }
                }
                .labelsHidden()
            }
            if model.form.datePreset == .custom {
                ViewThatFits {
                    HStack { dateFields }
                    VStack { dateFields }
                }
            }
            TextField("Tags", text: $model.form.tags).textFieldStyle(.roundedBorder)
            TextField("Users", text: $model.form.users).textFieldStyle(.roundedBorder)
            TextField("Domains", text: $model.form.domains).textFieldStyle(.roundedBorder)
            Text("Separate multiple values with commas or spaces.")
                .font(.caption).foregroundStyle(.secondary)
            if let validation = model.validation {
                Label(message(for: validation), systemImage: "exclamationmark.circle")
                    .foregroundStyle(.red).font(.subheadline)
                    .accessibilityIdentifier("advancedValidation")
            }
            Button(action: search) {
                Text("Search").frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(!model.canSearch)
            .accessibilityIdentifier("advancedSubmit")
        }
        .autocorrectionDisabled()
        .textInputAutocapitalization(.never)
        .padding(.top, 8)
    }

    @ViewBuilder private var dateFields: some View {
        let invalid = model.validation == .invalidCustomDateFormat || model.validation == .invalidCustomDateRange
        TextField("From", text: $model.form.customDateFrom, prompt: Text("YYYY-MM-DD HH:MM:SS"))
            .textFieldStyle(.roundedBorder)
            .overlay(RoundedRectangle(cornerRadius: 6).stroke(invalid ? .red : .clear))
        TextField("To", text: $model.form.customDateTo, prompt: Text("YYYY-MM-DD HH:MM:SS"))
            .textFieldStyle(.roundedBorder)
            .overlay(RoundedRectangle(cornerRadius: 6).stroke(invalid ? .red : .clear))
    }

    private func search() {
        model.search()
        if model.validation == nil { filtersExpanded = false }
    }

    private func title(for sort: AdvancedSearchForm.Sort) -> String {
        switch sort {
        case .score: String(localized: "Best match")
        case .popular: String(localized: "Popular")
        case .comments: String(localized: "Most commented")
        case .newest: String(localized: "Newest")
        }
    }

    private func title(for preset: AdvancedSearchForm.DatePreset) -> String {
        switch preset {
        case .anyTime: String(localized: "Any time")
        case .last24Hours: String(localized: "Last 24 hours")
        case .last3Days: String(localized: "Last 3 days")
        case .last7Days: String(localized: "Last 7 days")
        case .last30Days: String(localized: "Last 30 days")
        case .lastYear: String(localized: "Last year")
        case .custom: String(localized: "Custom range")
        }
    }

    private func message(for validation: AdvancedSearchValidation) -> String {
        switch validation {
        case .queryRequired: String(localized: "Enter a search phrase.")
        case .invalidCustomDateFormat: String(localized: "Use the date format YYYY-MM-DD HH:MM:SS.")
        case .invalidCustomDateRange: String(localized: "The start date must be before the end date.")
        case .invalid: String(localized: "Check the search filters.")
        }
    }
}
