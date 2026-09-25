import SwiftUI

/// The signed-in user's banner, avatar and name at the top of More (Android's More profile
/// header). The banner runs under the status bar and stretches when the page is pulled down.
struct MoreProfileHeader: View {
    let profile: Profile
    let open: () -> Void
    private let bannerHeight: CGFloat = 210
    private let avatarSize: CGFloat = 92

    var body: some View {
        Button(action: open) {
            VStack(alignment: .leading, spacing: 10) {
                banner
                HStack(alignment: .bottom, spacing: 14) {
                    AvatarView(url: profile.avatarURL, name: profile.username, size: avatarSize,
                               gender: profile.gender)
                        .padding(3)
                        .background(WykopTheme.background,
                                    in: RoundedRectangle(cornerRadius: avatarSize * 0.22 + 3, style: .continuous))
                    VStack(alignment: .leading, spacing: 3) {
                        Text(profile.username)
                            .font(.title2.bold())
                            .foregroundStyle(authorColor(profile.color))
                            .lineLimit(1)
                            .minimumScaleFactor(0.6)
                        if let joined {
                            Text(joined).font(.subheadline).foregroundStyle(.secondary)
                        }
                    }
                    .padding(.bottom, 12)
                    Spacer(minLength: 0)
                    Image(systemName: "chevron.right")
                        .font(.footnote.weight(.bold))
                        .foregroundStyle(.secondary)
                        .frame(width: 30, height: 30)
                        .background(WykopTheme.card, in: Circle())
                        .padding(.bottom, 16)
                }
                .padding(.horizontal, 20)
                .padding(.top, -(avatarSize / 2 + 10))
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Profile")
        .accessibilityValue(profile.username)
        .accessibilityAddTraits(.isButton)
        .accessibilityIdentifier("moreProfile")
    }

    private var banner: some View {
        Group {
            if let url = profile.bannerURL {
                RemoteImage(url: url, maxDimension: 1400) { MagicGradient() }
            } else {
                MagicGradient()
            }
        }
        .frame(height: bannerHeight)
        .frame(maxWidth: .infinity)
        // Keeps the status bar readable over bright banners and fades into the page.
        .overlay(alignment: .top) {
            LinearGradient(colors: [.black.opacity(0.35), .clear], startPoint: .top, endPoint: .bottom)
                .frame(height: 100)
        }
        .overlay(alignment: .bottom) {
            LinearGradient(colors: [.clear, WykopTheme.background], startPoint: .top, endPoint: .bottom)
                .frame(height: 70)
        }
        .clipped()
        .visualEffect { content, proxy in
            // Pulling down past the top grows the banner upward instead of revealing a gap.
            let stretch = max(0, proxy.frame(in: .scrollView).minY)
            return content.scaleEffect(1 + stretch / bannerHeight, anchor: .bottom)
        }
        .accessibilityHidden(true)
    }

    private var joined: String? {
        guard let raw = profile.memberSince, let date = Dates.parse(raw) else { return nil }
        return String(localized: "Joined \(RelativeDateTimeFormatter().localizedString(for: date, relativeTo: Date()))")
    }
}
