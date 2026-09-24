package pl.masslany.podkop.common.deeplink

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppDeepLinkParserTest {
    private val parser = AppDeepLinkParser()

    @Test
    fun parsesLoginCallbackFromQueryParameters() {
        val result = parser.parse("https://masslany.pl/wykop/connect?token=abc&rtoken=def")

        assertEquals(
            expected = AppDeepLink.LoginCallback(
                token = "abc",
                refreshToken = "def",
            ),
            actual = result,
        )
    }

    @Test
    fun parsesLoginCallbackFromFragmentParameters() {
        val result = parser.parse("https://masslany.pl/wykop/connect#token=abc&rtoken=def")

        assertEquals(
            expected = AppDeepLink.LoginCallback(
                token = "abc",
                refreshToken = "def",
            ),
            actual = result,
        )
    }

    @Test
    fun parsesLinkDetailsWithoutScheme() {
        val result = parser.parse("masslany.pl/wykop/link/99999999/some-text-after-digits")

        assertEquals(
            expected = AppDeepLink.LinkDetails(id = 99999999),
            actual = result,
        )
    }

    @Test
    fun parsesEntryDetails() {
        val result = parser.parse("https://masslany.pl/wykop/wpis/99999999/some-text-after-digits")

        assertEquals(
            expected = AppDeepLink.EntryDetails(id = 99999999),
            actual = result,
        )
    }

    @Test
    fun parsesPrivateMessagesInbox() {
        val result = parser.parse("https://masslany.pl/app/private-messages")

        assertEquals(
            expected = AppDeepLink.PrivateMessagesInbox,
            actual = result,
        )
    }

    @Test
    fun ignoresUnsupportedHost() {
        val result = parser.parse("https://example.com/wykop/link/99999999/test")

        assertNull(result)
    }
}
