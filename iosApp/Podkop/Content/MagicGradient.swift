import SwiftUI

/// A slowly drifting mesh of Wykop colors for the More tab's hero surfaces. It stays still when
/// Reduce Motion is on and redraws at a low frame rate, so it costs little while visible.
struct MagicGradient: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        if reduceMotion {
            mesh(at: 0)
        } else {
            TimelineView(.animation(minimumInterval: 1 / 30)) { timeline in
                mesh(at: timeline.date.timeIntervalSinceReferenceDate)
            }
        }
    }

    private func mesh(at time: Double) -> some View {
        let t = Float(time)
        // Only the middle row and column move, so the edges stay anchored to the frame.
        let points: [SIMD2<Float>] = [
            [0, 0], [0.5, 0], [1, 0],
            [0, 0.5 + 0.12 * sin(t * 0.45)],
            [0.5 + 0.18 * cos(t * 0.35), 0.5 + 0.16 * sin(t * 0.5)],
            [1, 0.5 + 0.12 * cos(t * 0.4)],
            [0, 1], [0.5, 1], [1, 1],
        ]
        // Warm Wykop colors with a violet accent; dark enough everywhere for white text on top.
        let violet = Color(red: 0.42, green: 0.24, blue: 0.72)
        let colors: [Color] = [
            WykopTheme.hotOrange, WykopTheme.nameBurgundy, WykopTheme.genderPink,
            WykopTheme.nameBurgundy, WykopTheme.hotOrange, violet,
            WykopTheme.favouriteGold, WykopTheme.nameBurgundy, violet,
        ]
        return MeshGradient(width: 3, height: 3, points: points, colors: colors, smoothsColors: true)
            .overlay(Color.black.opacity(0.18))
    }
}
