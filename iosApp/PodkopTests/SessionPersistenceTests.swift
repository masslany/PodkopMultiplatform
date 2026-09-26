import Security
import XCTest
import PodkopShared
@testable import Podkop

/// The sign-in must survive relaunches and updates, so its tokens have to reach the Keychain,
/// not only the shared layer's in-memory cache. This drives the real login-callback path and then
/// reads the Keychain directly, the way a fresh process would.
@MainActor
final class SessionPersistenceTests: XCTestCase {
    private let service = "pl.masslany.podkop.secure_storage"

    private func keychainValue(_ account: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne,
        ]
        var result: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
              let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    private func deleteKeychainValue(_ account: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
        ]
        SecItemDelete(query as CFDictionary)
    }

    override func tearDown() async throws {
        if ProcessInfo.processInfo.environment["KEEP_SESSION_PROBE"] == nil {
            deleteKeychainValue("API_TOKEN")
            deleteKeychainValue("REFRESH_TOKEN")
        }
    }

    func testLoginCallbackTokensAreWrittenToTheKeychain() async throws {
        let dependencies = AppDependencies.shared
        let callback = "https://masslany.pl/wykop/connect?token=test-access&rtoken=test-refresh"
        let intent: IOSLinkIntent = try await dependencies.adapter.call {
            dependencies.client.session.acceptUrl(url: callback, completion: $0)
        }
        XCTAssertEqual(intent.kind, "login")
        XCTAssertEqual(keychainValue("REFRESH_TOKEN"), "test-refresh",
                       "the refresh token decides whether the next launch is signed in")
        XCTAssertEqual(keychainValue("API_TOKEN"), "test-access")
    }
}
