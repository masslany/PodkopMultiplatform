import SwiftUI

import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {

        KoinHelperKt.doInitKoinIos()

        let key = Bundle.main.object(forInfoDictionaryKey: "WYKOP_KEY") as? String ?? ""
        let secret = Bundle.main.object(forInfoDictionaryKey: "WYKOP_SECRET") as? String ?? ""

        let helper = IOSDependencyHelper()
        helper.start(key: key, secret: secret)

        return true
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
