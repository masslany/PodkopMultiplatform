import SwiftUI

struct PrivacyPolicyView: View {
    var body: some View {
        PodkopList {
            Section { Text(.privacyIntro) }
            Section(.privacyServiceTitle) {
                Text(.privacyServiceBody)
                Link(.privacyWykopPolicy,
                     destination: URL(string: "https://wykop.pl/polityka-prywatnosci-i-cookies")!)
            }
            Section(.privacyDeviceTitle) { Text(.privacyDeviceBody) }
            Section(.privacyPermissionsTitle) { Text(.privacyPermissionsBody) }
            Section(.privacyNotificationsTitle) { Text(.privacyNotificationsBody) }
            Section(.privacyTrackingTitle) { Text(.privacyTrackingBody) }
        }
        .navigationTitle(.privacyTitle)
        .accessibilityIdentifier("privacyPolicy")
    }
}
