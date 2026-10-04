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
    fun parsesProfileAndTagOnAppHost() {
        assertEquals(
            expected = AppDeepLink.Profile(username = "some_user"),
            actual = parser.parse("https://masslany.pl/wykop/ludzie/some_user"),
        )
        assertEquals(
            expected = AppDeepLink.Tag(name = "heheszki"),
            actual = parser.parse("https://masslany.pl/wykop/tag/heheszki"),
        )
    }

    @Test
    fun parsesEntryOnContentHost() {
        val result = parser.parse("https://wykop.pl/wpis/84017695/nowa-aplikacja-wykopu-juz-jest")

        assertEquals(
            expected = AppDeepLink.EntryDetails(id = 84017695),
            actual = result,
        )
    }

    @Test
    fun parsesEntryCommentAnchorAsEntry() {
        assertEquals(AppDeepLink.EntryDetails(id = 123), parser.parse("https://wykop.pl/wpis/123/slug#456"))
        assertEquals(AppDeepLink.EntryDetails(id = 123), parser.parse("https://wykop.pl/wpis/123/komentarz/456"))
    }

    @Test
    fun parsesLinkCommentPathAsLink() {
        val result = parser.parse("https://wykop.pl/link/4394541/some-slug/komentarz/789/reply-slug")

        assertEquals(
            expected = AppDeepLink.LinkDetails(id = 4394541),
            actual = result,
        )
    }

    @Test
    fun parsesProfileWithTab() {
        val result = parser.parse("https://wykop.pl/ludzie/Some-User_1/wpisy/dodane")

        assertEquals(
            expected = AppDeepLink.Profile(username = "Some-User_1"),
            actual = result,
        )
    }

    @Test
    fun parsesTagWithTabAsLowercaseName() {
        val result = parser.parse("https://wykop.pl/tag/Heheszki/najlepsze")

        assertEquals(
            expected = AppDeepLink.Tag(name = "heheszki"),
            actual = result,
        )
    }

    @Test
    fun parsesLegacyTagUrls() {
        assertEquals(AppDeepLink.Tag(name = "heheszki"), parser.parse("https://www.wykop.pl/tag/wpisy/heheszki/"))
        assertEquals(AppDeepLink.Tag(name = "polska"), parser.parse("https://www.wykop.pl/tag/znaleziska/polska/"))
        assertEquals(AppDeepLink.Tag(name = "wpisy"), parser.parse("https://wykop.pl/tag/wpisy"))
    }

    @Test
    fun parsesContentHostVariants() {
        assertEquals(AppDeepLink.EntryDetails(id = 1), parser.parse("wykop.pl/wpis/1"))
        assertEquals(AppDeepLink.LinkDetails(id = 2), parser.parse("http://www.wykop.pl/link/2/old-slug/"))
        assertEquals(AppDeepLink.LinkDetails(id = 3), parser.parse("https://m.wykop.pl/link/3"))
        assertEquals(AppDeepLink.EntryDetails(id = 4), parser.parse("https://WYKOP.pl/WPIS/4"))
    }

    @Test
    fun ignoresLoginCallbackOnContentHost() {
        assertNull(parser.parse("https://wykop.pl/connect?token=abc&rtoken=def"))
        assertNull(parser.parse("https://wykop.pl/wykop/connect#token=abc&rtoken=def"))
        assertEquals(AppDeepLink.EntryDetails(id = 1), parser.parse("https://wykop.pl/wpis/1?token=abc&rtoken=def"))
    }

    @Test
    fun ignoresUnsupportedContentPaths() {
        assertNull(parser.parse("https://wykop.pl/"))
        assertNull(parser.parse("https://wykop.pl/regulamin"))
        assertNull(parser.parse("https://wykop.pl/link/dodaj"))
        assertNull(parser.parse("https://wykop.pl/wpis"))
        assertNull(parser.parse("https://wykop.pl/ludzie/"))
        assertNull(parser.parse("https://wykop.pl/tag/"))
        assertNull(parser.parse("https://wykop.pl/ludzie/some%20user"))
        assertNull(parser.parse("https://wykop.pl/mikroblog/hot"))
    }

    @Test
    fun ignoresLookalikeContentHosts() {
        assertNull(parser.parse("https://wykop.pl.evil.example/wpis/1"))
        assertNull(parser.parse("https://notwykop.pl/wpis/1"))
        assertNull(parser.parse("ftp://wykop.pl/wpis/1"))
    }

    @Test
    fun ignoresUnsupportedHost() {
        val result = parser.parse("https://example.com/wykop/link/99999999/test")

        assertNull(result)
    }

    @Test
    fun ignoresUnsupportedSchemeEvenForTrustedHost() {
        assertNull(parser.parse("http://masslany.pl/wykop/link/99999999/test"))
        assertNull(parser.parse("javascript://masslany.pl/wykop/connect?token=abc&rtoken=def"))
    }

    @Test
    fun recognizesOnlyHttpsAppHostForEmbeddedLogin() {
        kotlin.test.assertTrue(parser.isAppHost("https://masslany.pl/wykop/connect?error=denied"))
        kotlin.test.assertTrue(parser.isAppHost("https://MASSLANY.pl/anything"))
        kotlin.test.assertFalse(parser.isAppHost("http://masslany.pl/wykop/connect?token=a&rtoken=b"))
        kotlin.test.assertFalse(parser.isAppHost("https://wykop.pl/connect"))
        kotlin.test.assertFalse(parser.isAppHost("https://masslany.pl.evil.example/connect"))
        kotlin.test.assertFalse(parser.isAppHost(""))
    }
}
