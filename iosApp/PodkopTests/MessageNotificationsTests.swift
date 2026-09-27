import XCTest
@testable import Podkop

final class MessageNotificationsTests: XCTestCase {
    func testNotifiesOnlyWhenTheUnreadCountGrows() {
        XCTAssertNil(MessageNotifications.alert(previous: nil, current: 3), "the first check only seeds the count")
        XCTAssertNil(MessageNotifications.alert(previous: 3, current: 3))
        XCTAssertNil(MessageNotifications.alert(previous: 3, current: 1), "reading messages never notifies")
        XCTAssertEqual(MessageNotifications.alert(previous: 1, current: 2), .single)
        XCTAssertEqual(MessageNotifications.alert(previous: 1, current: 4), .multiple)
    }
}
