import SwiftUI

struct HitsView: View {
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
            AdaptiveControlRow {
                Menu {
                    ForEach(HitsModel.Sort.allCases, id: \.self) { sort in
                        Button(title(for: sort)) { model.select(sort) }
                    }
                } label: {
                    DropdownLabel(title: title(for: model.sort))
                }
                .accessibilityIdentifier("hitsSort")
                Button { pickingArchive = true } label: {
                    DropdownLabel(title: archiveTitle, systemImage: "calendar")
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("hitsArchive")
            }
        } rows: {
            PagedResourceRows(pager: model.pager, tab: tab, dependencies: dependencies)
        }
        .navigationTitle(.commonHits)
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
        guard let archive = model.archive else { return String(localized: .discoveryArchive) }
        return "\(Calendar.current.standaloneMonthSymbols[archive.month - 1]) \(String(archive.year))"
    }

    private func title(for sort: HitsModel.Sort) -> String {
        switch sort {
        case .all: String(localized: .discoveryAllTime)
        case .day: String(localized: .discoveryDay)
        case .week: String(localized: .discoveryWeek)
        case .month: String(localized: .discoveryMonth)
        case .year: String(localized: .discoveryYear)
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
                        .accessibilityLabel(.discoveryPreviousYear)
                    Text(String(year)).font(.title2.monospacedDigit()).frame(minWidth: 80)
                    Button { changeYear(1) } label: { Image(systemName: "chevron.right") }
                        .disabled(year >= maxYear)
                        .accessibilityLabel(.discoveryNextYear)
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
            .navigationTitle(.discoveryArchive)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(.commonCancel) { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button(.discoveryShow) { confirm(HitsArchive(year: year, month: month)) }
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
