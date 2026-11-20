package pl.masslany.podkop.features.resources

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.survey.registerVote
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState

/** Sends votes and applies each one to the listed item once the API accepts it. */
internal class ResourceVoting(
    private val linksRepository: LinksRepository,
    private val entriesRepository: EntriesRepository,
    private val store: ResourceItemsStore,
    private val logger: AppLogger,
) {
    /** Returns the link fetched again after the vote, or null when the vote or the fetch fails. */
    suspend fun voteLink(linkId: Int, voted: Boolean): ResourceItem? {
        val result = if (voted) {
            linksRepository.removeVoteOnLink(linkId = linkId)
        } else {
            linksRepository.voteOnLink(linkId = linkId)
        }
        if (result.isFailure) return null

        return linksRepository.getLink(linkId)
            .onSuccess { logger.debug("Link updated: $it") }
            .onFailure { logger.error("Link update fetch failed for id=$linkId", it) }
            .getOrNull()
            ?.data
    }

    suspend fun voteEntry(entryId: Int, voted: Boolean) {
        val result = if (voted) {
            entriesRepository.removeVoteUp(entryId)
        } else {
            entriesRepository.voteUp(entryId)
        }

        result.onSuccess {
            store.updateAs<EntryItemState>(entryId) { it.copy(voteState = it.voteState.afterVoteUp(voted)) }
        }
    }

    suspend fun voteEntrySurvey(entryId: Int, optionNumber: Int) {
        val canVote = store.findEntry(entryId)?.surveyState?.isVoteActionEnabled == true
        if (!canVote) return

        entriesRepository.voteSurvey(entryId = entryId, optionNumber = optionNumber).onSuccess {
            store.updateAs<EntryItemState>(entryId) { entry ->
                entry.surveyState
                    ?.let { entry.copy(surveyState = it.registerVote(optionNumber)) }
                    ?: entry
            }
        }
    }

    suspend fun voteEntryComment(entryId: Int, commentId: Int, voted: Boolean) {
        val result = if (voted) {
            entriesRepository.removeVoteUpComment(entryId = entryId, commentId = commentId)
        } else {
            entriesRepository.voteUpComment(entryId = entryId, commentId = commentId)
        }

        result.onSuccess {
            store.updateAs<EntryCommentItemState>(commentId) { it.copy(voteState = it.voteState.afterVoteUp(voted)) }
        }
    }

    suspend fun voteLinkCommentUp(linkId: Int, commentId: Int, voted: Boolean) {
        val result = if (voted) {
            linksRepository.removeVoteOnLinkComment(linkId = linkId, commentId = commentId)
        } else {
            linksRepository.voteOnLinkComment(linkId = linkId, commentId = commentId)
        }

        result.onSuccess {
            store.updateAs<LinkCommentItemState>(commentId) { it.copy(voteState = it.voteState.afterVoteUp(voted)) }
        }
    }

    suspend fun voteLinkCommentDown(linkId: Int, commentId: Int, voted: Boolean) {
        val result = if (voted) {
            linksRepository.removeVoteOnLinkComment(linkId = linkId, commentId = commentId)
        } else {
            linksRepository.voteDownOnLinkComment(linkId = linkId, commentId = commentId)
        }

        result.onSuccess {
            store.updateAs<LinkCommentItemState>(commentId) { it.copy(voteState = it.voteState.afterVoteDown(voted)) }
        }
    }
}

private fun VoteState.afterVoteUp(wasVoted: Boolean): VoteState =
    if (wasVoted) removeVoteUp() else increaseVoteUp()

private fun VoteState.afterVoteDown(wasVoted: Boolean): VoteState =
    if (wasVoted) removeVoteDown() else increaseVoteDown()
