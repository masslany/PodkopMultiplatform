import SwiftUI

struct AdvancedSearchView: View {
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
                    Text(.searchFilters).font(.headline)
                }
                if model.hasSearched {
                    if let total = model.results.total, model.results.phase == .loaded {
                        Text(.searchResults(total)).font(.subheadline).foregroundStyle(.secondary)
                    }
                    PagedResourceRows(pager: model.results, tab: tab, dependencies: dependencies,
                                      emptyTitle: .commonNoResults)
                } else {
                    ContentUnavailableView(.searchSetFiltersSearch, systemImage: "magnifyingglass")
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 20)
            .frame(maxWidth: 900)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await model.refresh() }
        .navigationTitle(.searchAdvancedSearch)
        .task { model.resume() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
    }

    @ViewBuilder private var filters: some View {
        VStack(alignment: .leading, spacing: 14) {
            TextField(String(localized: .searchSearchPhrase), text: $model.form.query)
                .textFieldStyle(.roundedBorder)
                .submitLabel(.search)
                .onSubmit(search)
                .accessibilityIdentifier("advancedQuery")
            LabeledContent(String(localized: .searchSort)) {
                Picker(.searchSort, selection: $model.form.sort) {
                    ForEach(AdvancedSearchForm.Sort.allCases, id: \.self) { Text(title(for: $0)).tag($0) }
                }
                .labelsHidden()
            }
            LabeledContent(String(localized: .searchMinimumVotes)) {
                Picker(.searchMinimumVotes, selection: $model.form.minimumVotes) {
                    ForEach(AdvancedSearchForm.voteOptions, id: \.self) { value in
                        Text(value.map { "\($0)+" } ?? String(localized: .commonAll)).tag(value)
                    }
                }
                .labelsHidden()
            }
            LabeledContent(String(localized: .searchDate)) {
                Picker(.searchDate, selection: $model.form.datePreset) {
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
            TextField(String(localized: .commonTags), text: $model.form.tags).textFieldStyle(.roundedBorder)
            TextField(String(localized: .commonUsers), text: $model.form.users).textFieldStyle(.roundedBorder)
            TextField(String(localized: .commonDomains), text: $model.form.domains).textFieldStyle(.roundedBorder)
            Text(.searchSeparateMultipleValuesCommas)
                .font(.caption).foregroundStyle(.secondary)
            if let validation = model.validation {
                Label(message(for: validation), systemImage: "exclamationmark.circle")
                    .foregroundStyle(.red).font(.subheadline)
                    .accessibilityIdentifier("advancedValidation")
            }
            Button(action: search) {
                Text(.commonSearch).frame(maxWidth: .infinity)
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
        TextField(String(localized: .searchFrom), text: $model.form.customDateFrom, prompt: Text(verbatim: "YYYY-MM-DD HH:MM:SS"))
            .textFieldStyle(.roundedBorder)
            .overlay(RoundedRectangle(cornerRadius: 6).stroke(invalid ? .red : .clear))
        TextField(String(localized: .searchTo), text: $model.form.customDateTo, prompt: Text(verbatim: "YYYY-MM-DD HH:MM:SS"))
            .textFieldStyle(.roundedBorder)
            .overlay(RoundedRectangle(cornerRadius: 6).stroke(invalid ? .red : .clear))
    }

    private func search() {
        model.search()
        if model.validation == nil { filtersExpanded = false }
    }

    private func title(for sort: AdvancedSearchForm.Sort) -> String {
        switch sort {
        case .score: String(localized: .searchBestMatch)
        case .popular: String(localized: .searchPopular)
        case .comments: String(localized: .searchMostCommented)
        case .newest: String(localized: .commonNewest)
        }
    }

    private func title(for preset: AdvancedSearchForm.DatePreset) -> String {
        switch preset {
        case .anyTime: String(localized: .searchAnyTime)
        case .last24Hours: String(localized: .searchLast24Hours)
        case .last3Days: String(localized: .searchLast3Days)
        case .last7Days: String(localized: .searchLast7Days)
        case .last30Days: String(localized: .searchLast30Days)
        case .lastYear: String(localized: .searchLastYear)
        case .custom: String(localized: .searchCustomRange)
        }
    }

    private func message(for validation: AdvancedSearchValidation) -> String {
        switch validation {
        case .queryRequired: String(localized: .searchEnterSearchPhrase)
        case .invalidCustomDateFormat: String(localized: .searchUseDateFormatYYYY)
        case .invalidCustomDateRange: String(localized: .searchStartDateMustBefore)
        case .invalid: String(localized: .searchCheckSearchFilters)
        }
    }
}
