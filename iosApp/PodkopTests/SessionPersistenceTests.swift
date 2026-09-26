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
        // The host app caches its client in AppDependencies.shared. Earlier bridge tests
        // close the singleton client, so that cached reference may already be closed.
        // Acquire the current client for this test and own its cleanup instead.
        let client = PodkopClient.companion.create()
        let adapter = BridgeAdapter()
        defer { adapter.close(); client.close() }
        let callback = "https://masslany.pl/wykop/connect?token=test-access&rtoken=test-refresh"
        let completed = expectation(description: "login callback completes")
        var outcome: Result<IOSLinkIntent, Error>?
        let task = Task { @MainActor in
            do {
                let intent: IOSLinkIntent = try await adapter.call {
                    client.session.acceptUrl(url: callback, completion: $0)
                }
                outcome = .success(intent)
            } catch {
                outcome = .failure(error)
            }
            completed.fulfill()
        }
        defer { task.cancel() }
        // XCTest's deadline does not depend on the shared coroutine returning. Closing
        // the adapter and cancelling the task also release a pending Swift continuation.
        await fulfillment(of: [completed], timeout: 5)
        guard let outcome else { return }
        XCTAssertEqual(try outcome.get().kind, "login")
        XCTAssertEqual(keychainValue("REFRESH_TOKEN"), "test-refresh",
                       "the refresh token decides whether the next launch is signed in")
        XCTAssertEqual(keychainValue("API_TOKEN"), "test-access")
    }
}
