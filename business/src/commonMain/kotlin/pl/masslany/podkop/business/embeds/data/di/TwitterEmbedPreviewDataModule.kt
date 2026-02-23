package pl.masslany.podkop.business.embeds.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.embeds.data.main.TwitterEmbedPreviewRepositoryImpl
import pl.masslany.podkop.business.embeds.data.main.parser.TwitterSyndicationParser
import pl.masslany.podkop.business.embeds.data.main.parser.TwitterSyndicationParserImpl
import pl.masslany.podkop.business.embeds.data.main.token.TwitterSyndicationTokenGenerator
import pl.masslany.podkop.business.embeds.data.main.token.TwitterSyndicationTokenGeneratorImpl
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository

val twitterEmbedPreviewDataModule = module {
    single<TwitterSyndicationParser> { TwitterSyndicationParserImpl() }
    single<TwitterSyndicationTokenGenerator> { TwitterSyndicationTokenGeneratorImpl() }

    single<TwitterEmbedPreviewRepository> {
        TwitterEmbedPreviewRepositoryImpl(
            twitterDataSource = get(),
            dispatcherProvider = get(),
            twitterSyndicationParser = get(),
            twitterSyndicationTokenGenerator = get(),
        )
    }
}
