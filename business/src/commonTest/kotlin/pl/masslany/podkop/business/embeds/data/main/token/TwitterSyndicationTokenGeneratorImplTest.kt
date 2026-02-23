package pl.masslany.podkop.business.embeds.data.main.token

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TwitterSyndicationTokenGeneratorImplTest {
    private val sut = TwitterSyndicationTokenGeneratorImpl()

    @Test
    fun `generates stable expected tokens for known tweet ids`() {
        assertEquals("bhi2ay3f2", sut.generate(1))
        assertEquals("36vn1gy6m", sut.generate(10))
        assertEquals("4wsn6kf684ca4du4pqcwh", sut.generate(2025929093305217210))
    }

    @Test
    fun `generated token removes separators and zero digits`() {
        val token = sut.generate(1234567890123456789)

        assertTrue(token.isNotBlank())
        assertFalse(token.contains('.'))
        assertFalse(token.contains('0'))
    }

    @Test
    fun `different ids generate different tokens`() {
        assertNotEquals(sut.generate(100), sut.generate(101))
    }
}
