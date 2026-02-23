package pl.masslany.podkop.business.embeds.domain.main

import pl.masslany.podkop.business.embeds.domain.models.TwitterEmbedPreview

interface TwitterEmbedPreviewRepository {
    suspend fun getTweet(url: String): Result<TwitterEmbedPreview>
}
