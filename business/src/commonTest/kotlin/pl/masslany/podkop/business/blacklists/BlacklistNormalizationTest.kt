package pl.masslany.podkop.business.blacklists

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistNormalization

class BlacklistNormalizationTest {
    @Test
    fun `users keep their case but lose the mention prefix`() {
        assertEquals("Ewa-Żółw", BlacklistNormalization.user("  @Ewa-Żółw "))
    }

    @Test
    fun `tags lose the hash and are lowercased`() {
        assertEquals("technologia", BlacklistNormalization.tag(" #Technologia"))
    }

    @Test
    fun `domains are trimmed and lowercased`() {
        assertEquals("example.com", BlacklistNormalization.domain(" Example.COM "))
    }
}
