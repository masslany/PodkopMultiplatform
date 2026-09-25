import SwiftUI
import PodkopShared

struct NativeRoot: View {
    let dependencies: AppDependencies
    @Environment(\.scenePhase) private var scenePhase
    @State private var sceneID = UUID()
    @State private var externalPage: ExternalPage?

    private var session: SessionModel { dependencies.session }
    private var router: AppRouter { dependencies.router }

    var body: some View {
        Group {
            switch session.phase {
            case .initializing:
                NativeSplashView()
                    .transition(.opacity)
            case .missingConfiguration:
                startupProblem(
                    title: String(localized: "Configuration required"),
                    explanation: String(localized: "Add the app configuration and try again.")
                )
            case .error:
                startupProblem(
                    title: String(localized: "Could not start Podkop"),
                    explanation: String(localized: "Check your connection and try again.")
                )
            case .ready:
                #if DEBUG
                if ProcessInfo.processInfo.arguments.contains("content") {
                    ContentFixtureGallery()
                } else {
                    tabs
                }
                #else
                tabs
                #endif
            }
        }
        .environment(\.mediaLoader, dependencies.mediaLoader)
        .environment(\.openURL, OpenURLAction { url in
            guard ["http", "https"].contains(url.scheme?.lowercased() ?? "") else {
                return .systemAction(url)
            }
            externalPage = ExternalPage(url: url)
            return .handled
        })
        .preferredColorScheme(session.theme.colorScheme)
        .animation(.easeOut(duration: 0.25), value: session.phase)
        .task { session.startIfNeeded() }
        .onChange(of: scenePhase, initial: true) { _, phase in
            dependencies.scene(sceneID, active: phase == .active)
        }
        .onDisappear { dependencies.scene(sceneID, active: false) }
        .onOpenURL { url in Task { await session.accept(url) } }
        .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
            if let url = activity.webpageURL { Task { await session.accept(url) } }
        }
        .sheet(item: Binding(get: { router.sheet }, set: { value in
            if value == nil { router.dismissSheet() } else { router.sheet = value }
        })) { sheet in
            switch sheet {
            case .login:
                LoginSheet(session: session) { router.dismissSheet() }
            case .composer:
                if let intent = router.composerIntent {
                    NativeComposerView(intent: intent, seed: router.composerSeed,
                                       dependencies: dependencies)
                }
            }
        }
        .sheet(item: $externalPage) { page in NativeSafariView(url: page.url) }
        .alert(item: Binding(get: { router.alert }, set: { router.alert = $0 })) { alert in
            Alert(title: Text(alert.title), message: Text(alert.message), dismissButton: .default(Text("OK")))
        }
        .overlay(alignment: .top) {
            if let message = session.banner ?? router.banner {
                HStack {
                    Text(message)
                    Spacer()
                    Button("Dismiss") { session.banner = nil; router.banner = nil }
                }
                .padding()
                .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
                .padding()
                .accessibilityIdentifier("appBanner")
            }
        }
    }

    private var tabs: some View {
                TabView(selection: Binding(get: { router.selectedTab }, set: { router.selectedTab = $0 })) {
                    ForEach(AppTab.allCases) { tab in
                        TabContent(tab: tab, dependencies: dependencies)
                            .tabItem { Label(tab.title, image: tab.image) }
                            .tag(tab)
                    }
                }
    }

    private func startupProblem(title: String, explanation: String) -> some View {
        ContentUnavailableView {
            Label(title, systemImage: "exclamationmark.triangle")
        } description: {
            Text(explanation)
        } actions: {
            Button("Retry") { Task { await session.retry() } }
                .buttonStyle(.borderedProminent)
        }
    }
}

private struct ExternalPage: Identifiable {
    let id = UUID()
    let url: URL
}
