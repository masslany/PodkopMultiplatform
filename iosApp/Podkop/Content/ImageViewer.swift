import SwiftUI
import UIKit

/// Full-screen image with pinch and double-tap zoom, like the Photos app: swipe down to close,
/// tap to show or hide the controls. Save, copy and share sit in a bar at the bottom.
struct ImageViewerScreen: View {
    let bytes: Data
    let key: String
    @Environment(\.dismiss) private var dismiss
    @State private var image: UIImage?
    @State private var message: String?
    @State private var chromeVisible = true
    /// 0 at rest, 1 when the drag has gone far enough to close.
    @State private var dragProgress: CGFloat = 0

    var body: some View {
        ZStack {
            Color.black.opacity(1 - dragProgress * 0.7).ignoresSafeArea()
            if let image {
                ZoomableImage(image: image,
                              onTap: { withAnimation(.easeInOut(duration: 0.2)) { chromeVisible.toggle() } },
                              onDrag: { dragProgress = $0 },
                              onDismiss: { dismiss() })
                    .ignoresSafeArea()
                    .accessibilityLabel(.commonImage)
            } else {
                ProgressView(.contentLoadingImage).tint(.white).foregroundStyle(.white)
            }
        }
        .overlay(alignment: .topLeading) {
            if chromeVisible && dragProgress == 0 {
                Button { dismiss() } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(width: 44, height: 44)
                        .background(.black.opacity(0.55), in: Circle())
                        .overlay(Circle().strokeBorder(.white.opacity(0.25)))
                }
                .padding(.leading, 16)
                .padding(.top, 8)
                .accessibilityLabel(.contentClose)
                .transition(.opacity)
            }
        }
        .overlay(alignment: .bottom) {
            if chromeVisible && dragProgress == 0 {
                VStack(spacing: 10) {
                    if let message {
                        Text(message)
                            .font(.footnote)
                            .foregroundStyle(.white)
                            .padding(.horizontal, 12).padding(.vertical, 6)
                            .background(.black.opacity(0.6), in: Capsule())
                            .accessibilityIdentifier("imageMessage")
                    }
                    ImageExportControls(data: bytes, style: .overlay) { message = $0 }
                }
                .padding(.bottom, 12)
                .transition(.opacity)
            }
        }
        .statusBarHidden(!chromeVisible)
        .preferredColorScheme(.dark)
        .presentationBackground(.clear)
        .task(id: key) {
            let bytes = bytes
            let key = key
            let decoded = await Task.detached(priority: .utility) {
                ImageDecoder.shared.image(from: bytes, key: key, maxDimension: 2048)
            }.value
            if !Task.isCancelled { image = decoded }
        }
    }
}

private struct ZoomableImage: UIViewRepresentable {
    let image: UIImage
    let onTap: () -> Void
    let onDrag: (CGFloat) -> Void
    let onDismiss: () -> Void

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> UIScrollView {
        let scroll = UIScrollView()
        scroll.minimumZoomScale = 1
        scroll.maximumZoomScale = 4
        scroll.delegate = context.coordinator
        scroll.showsVerticalScrollIndicator = false
        scroll.showsHorizontalScrollIndicator = false
        scroll.contentInsetAdjustmentBehavior = .never
        scroll.backgroundColor = .clear
        let imageView = UIImageView(image: image)
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        scroll.addSubview(imageView)
        NSLayoutConstraint.activate([
            imageView.leadingAnchor.constraint(equalTo: scroll.contentLayoutGuide.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: scroll.contentLayoutGuide.trailingAnchor),
            imageView.topAnchor.constraint(equalTo: scroll.contentLayoutGuide.topAnchor),
            imageView.bottomAnchor.constraint(equalTo: scroll.contentLayoutGuide.bottomAnchor),
            imageView.widthAnchor.constraint(equalTo: scroll.frameLayoutGuide.widthAnchor),
            imageView.heightAnchor.constraint(equalTo: scroll.frameLayoutGuide.heightAnchor)
        ])
        context.coordinator.imageView = imageView
        context.coordinator.scrollView = scroll

        let doubleTap = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.doubleTapped(_:)))
        doubleTap.numberOfTapsRequired = 2
        scroll.addGestureRecognizer(doubleTap)
        let tap = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.tapped))
        tap.require(toFail: doubleTap)
        scroll.addGestureRecognizer(tap)
        let pan = UIPanGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.panned(_:)))
        pan.delegate = context.coordinator
        scroll.addGestureRecognizer(pan)
        return scroll
    }

    func updateUIView(_ scroll: UIScrollView, context: Context) {
        context.coordinator.parent = self
        context.coordinator.imageView?.image = image
    }

    final class Coordinator: NSObject, UIScrollViewDelegate, UIGestureRecognizerDelegate {
        var parent: ZoomableImage
        weak var imageView: UIImageView?
        weak var scrollView: UIScrollView?

        init(_ parent: ZoomableImage) { self.parent = parent }

        func viewForZooming(in scrollView: UIScrollView) -> UIView? { imageView }

        @objc func tapped() { parent.onTap() }

        @objc func doubleTapped(_ gesture: UITapGestureRecognizer) {
            guard let scroll = scrollView, let imageView else { return }
            if scroll.zoomScale > scroll.minimumZoomScale {
                scroll.setZoomScale(scroll.minimumZoomScale, animated: true)
            } else {
                let point = gesture.location(in: imageView)
                let size = CGSize(width: scroll.bounds.width / 2.5, height: scroll.bounds.height / 2.5)
                scroll.zoom(to: CGRect(x: point.x - size.width / 2, y: point.y - size.height / 2,
                                       width: size.width, height: size.height), animated: true)
            }
        }

        /// Only a mostly vertical drag on an unzoomed image closes the viewer.
        func gestureRecognizerShouldBegin(_ gesture: UIGestureRecognizer) -> Bool {
            guard let pan = gesture as? UIPanGestureRecognizer, let scroll = scrollView else { return true }
            guard scroll.zoomScale <= scroll.minimumZoomScale + 0.01 else { return false }
            let velocity = pan.velocity(in: scroll)
            return abs(velocity.y) > abs(velocity.x) * 1.2
        }

        @objc func panned(_ gesture: UIPanGestureRecognizer) {
            guard let imageView, let scroll = scrollView else { return }
            let translation = gesture.translation(in: scroll)
            let progress = min(1, abs(translation.y) / 240)
            switch gesture.state {
            case .changed:
                let scale = 1 - progress * 0.25
                imageView.transform = CGAffineTransform(translationX: translation.x, y: translation.y)
                    .scaledBy(x: scale, y: scale)
                parent.onDrag(progress)
            case .ended, .cancelled, .failed:
                let velocity = gesture.velocity(in: scroll).y
                if gesture.state == .ended && (abs(translation.y) > 120 || abs(velocity) > 900) {
                    UIView.animate(withDuration: 0.2, animations: {
                        imageView.transform = CGAffineTransform(translationX: translation.x,
                                                                y: translation.y > 0 ? scroll.bounds.height : -scroll.bounds.height)
                        imageView.alpha = 0
                    }, completion: { _ in self.parent.onDismiss() })
                    parent.onDrag(1)
                } else {
                    UIView.animate(withDuration: 0.25, delay: 0, usingSpringWithDamping: 0.85,
                                   initialSpringVelocity: 0) {
                        imageView.transform = .identity
                    }
                    parent.onDrag(0)
                }
            default:
                break
            }
        }
    }
}
