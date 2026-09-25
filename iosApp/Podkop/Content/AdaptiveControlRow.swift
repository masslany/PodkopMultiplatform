import SwiftUI

/// A row of sort/filter controls that stays on one line when it fits and stacks its controls
/// otherwise, so large text never breaks a label mid-word. Trailing controls sit at the far end
/// of the row, or below the others when stacked.
struct AdaptiveControlRow<Leading: View, Trailing: View>: View {
    @ViewBuilder var leading: () -> Leading
    @ViewBuilder var trailing: () -> Trailing

    init(@ViewBuilder leading: @escaping () -> Leading,
         @ViewBuilder trailing: @escaping () -> Trailing = { EmptyView() }) {
        self.leading = leading
        self.trailing = trailing
    }

    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(spacing: 8) {
                leading()
                Spacer(minLength: 0)
                trailing()
            }
            VStack(alignment: .leading, spacing: 8) {
                leading()
                trailing()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}
