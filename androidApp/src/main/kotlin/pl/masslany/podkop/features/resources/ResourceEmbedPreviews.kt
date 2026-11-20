package pl.masslany.podkop.features.resources

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.StreamableEmbedState
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
import pl.masslany.podkop.common.models.embed.toTwitterEmbedPreviewState
import pl.masslany.podkop.common.navigation.AppNavigator

/**
 * Handles a tap on an embed: tweets and Streamable videos load inline, everything else opens in the
 * browser. [updateEmbedState] stores the loaded state on whichever list holds the item.
 */
internal class ResourceEmbedPreviews(
    private val appNavigator: AppNavigator,
    private val twitterEmbedPreviewRepository: TwitterEmbedPreviewRepository,
    private val streamableEmbedPlayback: StreamableEmbedPlayback,
    private val logger: AppLogger,
    private val updateEmbedState: (itemId: Int, transform: (EmbedContentState?) -> EmbedContentState?) -> Unit,
) {
    fun onPreviewClicked(scope: CoroutineScope, itemId: Int, state: EmbedContentState) {
        when (state.type) {
            EmbedContentType.Youtube,
            EmbedContentType.Other,
            -> appNavigator.openExternalLink(state.url)

            EmbedContentType.Streamable -> {
                // Playing and Error reload too: a fresh signed url fixes an expired or stalled stream.
                if (state.streamableState == StreamableEmbedState.Loading) return
                scope.launch { playStreamable(itemId, state) }
            }

            EmbedContentType.Twitter -> when (state.twitterState) {
                TwitterEmbedState.Preview -> scope.launch { loadTweet(itemId, state) }

                TwitterEmbedState.Loading -> Unit

                null,
                is TwitterEmbedState.Loaded,
                TwitterEmbedState.Error,
                -> appNavigator.openExternalLink(state.url)
            }
        }
    }

    private suspend fun playStreamable(itemId: Int, state: EmbedContentState) {
        if (state.streamableState == null || !streamableEmbedPlayback.isInlinePlaybackEnabled()) {
            appNavigator.openExternalLink(state.url)
            return
        }
        updateEmbedState(itemId) { it.updateStreamableEmbedStateIfMatches(state.key, StreamableEmbedState.Loading) }
        val resolved = streamableEmbedPlayback.resolve(state.url)
        updateEmbedState(itemId) { it.updateStreamableEmbedStateIfMatches(state.key, resolved) }
    }

    private suspend fun loadTweet(itemId: Int, state: EmbedContentState) {
        updateEmbedState(itemId) { it.updateTwitterEmbedStateIfMatches(state.key, TwitterEmbedState.Loading) }
        val loaded: TwitterEmbedState = twitterEmbedPreviewRepository.getTweet(state.url).fold(
            onSuccess = { tweet -> TwitterEmbedState.Loaded(tweet.toTwitterEmbedPreviewState()) },
            onFailure = { error ->
                logger.error(
                    message = "Twitter embed preview fetch failed for itemId=$itemId url=${state.url}",
                    throwable = error,
                )
                TwitterEmbedState.Error
            },
        )
        updateEmbedState(itemId) { it.updateTwitterEmbedStateIfMatches(state.key, loaded) }
    }
}

internal fun EmbedContentState?.updateTwitterEmbedStateIfMatches(
    embedKey: String,
    newState: TwitterEmbedState,
): EmbedContentState? {
    val current = this ?: return null
    if (current.type != EmbedContentType.Twitter) return current
    if (current.key != embedKey) return current

    return current.copy(twitterState = newState)
}

internal fun EmbedContentState?.updateStreamableEmbedStateIfMatches(
    embedKey: String,
    newState: StreamableEmbedState,
): EmbedContentState? {
    val current = this ?: return null
    if (current.type != EmbedContentType.Streamable) return current
    if (current.key != embedKey) return current

    return current.copy(streamableState = newState)
}
