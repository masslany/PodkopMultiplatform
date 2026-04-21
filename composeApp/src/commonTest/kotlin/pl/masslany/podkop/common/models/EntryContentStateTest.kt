package pl.masslany.podkop.common.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EntryContentStateTest {

    @Test
    fun `to entry content state keeps list markdown content intact`() {
        val state = assertIs<EntryContentState.Content>(
            "- first item\n2. second item".toEntryContentState(isDownVoted = false),
        )

        assertEquals("- first item\n2. second item", state.content)
    }

    @Test
    fun `to entry content state highlights tags and profiles`() {
        val state = assertIs<EntryContentState.Content>(
            "@user #tag".toEntryContentState(isDownVoted = false),
        )

        assertEquals("[@user](@user) [#tag](#tag)", state.content)
    }

    @Test
    fun `to entry content state escapes standalone dash separators`() {
        val state = assertIs<EntryContentState.Content>(
            "-------------\n#maszprawo - tresc\n-------------".toEntryContentState(isDownVoted = false),
        )

        assertEquals(
            "\\-------------\n[#maszprawo](#maszprawo) - tresc\n\\-------------",
            state.content,
        )
    }
}
