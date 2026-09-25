import SwiftUI

/// The signed-out hero on More: the shovel mark over a drifting Wykop gradient and a sign-in
/// button (Android shows a placeholder avatar with "Zaloguj się").
struct MoreSignInCard: View {
    let signIn: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            Image("TabUpcoming")
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 30, height: 30)
                .frame(width: 68, height: 68)
                .background(.white.opacity(0.18), in: Circle())
                .overlay(Circle().strokeBorder(.white.opacity(0.35), lineWidth: 1))
                .accessibilityHidden(true)
            Text(.moreSignInDigComment)
                .font(.subheadline)
                .multilineTextAlignment(.center)
                .opacity(0.9)
            Button(action: signIn) {
                Text(.commonSignIn)
                    .font(.headline)
                    .foregroundStyle(.black)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 12)
                    .background(.white, in: Capsule())
                    .shadow(color: .black.opacity(0.2), radius: 8, y: 4)
            }
            .buttonStyle(.plain)
            .padding(.top, 6)
        }
        .foregroundStyle(.white)
        .frame(maxWidth: .infinity)
        .padding(.vertical, 28)
        .padding(.horizontal, 24)
        .background(MagicGradient())
        .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
    }
}
