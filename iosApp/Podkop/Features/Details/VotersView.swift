import SwiftUI

struct VotersSheet: View {
    let target: VoterTarget
    let dependencies: AppDependencies
    @State private var model: VoterModel
    @Environment(\.dismiss) private var dismiss

    init(target: VoterTarget, dependencies: AppDependencies) {
        self.target = target
        self.dependencies = dependencies
        _model = State(initialValue: VoterModel(target: target, loader: dependencies.voterLoader))
    }

    var body: some View {
        NavigationStack {
            List {
                ForEach(model.voters) { voter in
                    Button { dismiss(); dependencies.router.navigate(.user(voter.username)) } label: {
                        HStack {
                            Circle().fill(.blue.opacity(0.15)).frame(width: 32, height: 32)
                                .overlay(Text(String(voter.username.prefix(1)).uppercased()))
                            Text(voter.username)
                            if voter.verified { Image(systemName: "checkmark.seal.fill") }
                            if let reason = voter.reason { Text(reason).foregroundStyle(.secondary) }
                        }
                    }
                    .onAppear { if voter.username == model.voters.last?.username { model.load() } }
                }
                if model.loading { ProgressView() }
                if model.failed { Button(.commonRetry) { model.load() } }
                if model.exhausted && model.voters.isEmpty { Text(.commonNothingHereYet) }
            }
            .navigationTitle(.detailsVoters)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) { Button(.detailsDone) { dismiss() } }
            }
        }
        .task { model.load() }
        .onDisappear { model.stop() }
    }
}
