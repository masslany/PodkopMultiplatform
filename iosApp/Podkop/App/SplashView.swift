import SwiftUI

/// Continues the launch storyboard (same icon, size and position) until startup is ready,
/// so there is no visible jump between the system launch screen and the app.
struct SplashView: View {
    @State private var showsProgress = false

    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground)
            Image("SplashIcon")
                .resizable()
                .frame(width: 120, height: 120)
                .accessibilityLabel(Text(verbatim: "Podkop"))
            // Only a slow start earns a spinner; it sits below the icon without moving it.
            ProgressView()
                .offset(y: 100)
                .opacity(showsProgress ? 1 : 0)
                .accessibilityHidden(!showsProgress)
        }
        .ignoresSafeArea()
        .task {
            try? await Task.sleep(for: .seconds(1.5))
            withAnimation(.easeIn(duration: 0.3)) { showsProgress = true }
        }
        .accessibilityIdentifier("splash")
    }
}
