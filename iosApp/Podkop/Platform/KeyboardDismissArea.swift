import SwiftUI
import UIKit

/// Dismisses the keyboard for taps outside a text editor without consuming button taps.
struct KeyboardDismissArea: UIViewRepresentable {
    func makeUIView(context: Context) -> KeyboardDismissView { KeyboardDismissView() }
    func updateUIView(_ uiView: KeyboardDismissView, context: Context) {}
    static func dismantleUIView(_ uiView: KeyboardDismissView, coordinator: Void) { uiView.detach() }
}

final class KeyboardDismissView: UIView, UIGestureRecognizerDelegate {
    private var tap: UITapGestureRecognizer?

    override func didMoveToWindow() {
        super.didMoveToWindow()
        detach()
        guard let window else { return }
        let tap = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard))
        tap.cancelsTouchesInView = false
        tap.delegate = self
        window.addGestureRecognizer(tap)
        self.tap = tap
    }

    func detach() {
        if let tap { tap.view?.removeGestureRecognizer(tap) }
        tap = nil
    }

    @objc private func dismissKeyboard() { window?.endEditing(true) }

    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        guard bounds.contains(touch.location(in: self)) else { return false }
        var view = touch.view
        while let current = view {
            if current is UITextView || current is UITextField || current is UIControl { return false }
            view = current.superview
        }
        return true
    }

    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer,
                           shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool { true }
}
