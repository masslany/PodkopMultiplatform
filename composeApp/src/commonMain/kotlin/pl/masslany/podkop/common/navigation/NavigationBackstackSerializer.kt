package pl.masslany.podkop.common.navigation

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaAttachBottomSheetScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaPickLocalScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaUrlDialogScreen
import pl.masslany.podkop.features.about.AboutAppScreen
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreen
import pl.masslany.podkop.features.debug.DebugScreen
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.favorites.FavoritesScreen
import pl.masslany.podkop.features.hits.HitsScreen
import pl.masslany.podkop.features.imageviewer.ImageViewerScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.linksubmission.AddLinkScreen
import pl.masslany.podkop.features.linksubmission.LinkDraftScreen
import pl.masslany.podkop.features.more.MoreScreen
import pl.masslany.podkop.features.notifications.NotificationsScreen
import pl.masslany.podkop.features.observed.ObservedScreen
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.privatemessages.NewConversationScreen
import pl.masslany.podkop.features.privatemessages.PrivateMessagesScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.rank.RankScreen
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogScreen
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogScreen
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetScreen
import pl.masslany.podkop.features.search.AdvancedSearchScreen
import pl.masslany.podkop.features.search.SearchScreen
import pl.masslany.podkop.features.settings.SettingsScreen
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen

internal object NavigationBackstackSerializer {
    private val backStackSerializer = ListSerializer(PolymorphicSerializer(NavTarget::class))

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(NavTarget::class) {
                subclass(HomeScreen::class, HomeScreen.serializer())
                subclass(GenericDialog::class, GenericDialog.serializer())
                subclass(AddLinkScreen::class, AddLinkScreen.serializer())
                subclass(LinkDraftScreen::class, LinkDraftScreen.serializer())
                subclass(LinksScreen::class, LinksScreen.serializer())
                subclass(UpcomingScreen::class, UpcomingScreen.serializer())
                subclass(EntriesScreen::class, EntriesScreen.serializer())
                subclass(MoreScreen::class, MoreScreen.serializer())
                subclass(DebugScreen::class, DebugScreen.serializer())
                subclass(ProfileScreen::class, ProfileScreen.serializer())
                subclass(RankScreen::class, RankScreen.serializer())
                subclass(ComposerMediaUrlDialogScreen::class, ComposerMediaUrlDialogScreen.serializer())
                subclass(ComposerMediaPickLocalScreen::class, ComposerMediaPickLocalScreen.serializer())
                subclass(ComposerMediaAttachBottomSheetScreen::class, ComposerMediaAttachBottomSheetScreen.serializer())
                subclass(FavoritesScreen::class, FavoritesScreen.serializer())
                subclass(NotificationsScreen::class, NotificationsScreen.serializer())
                subclass(ObservedScreen::class, ObservedScreen.serializer())
                subclass(ResourceScreenshotPreviewDialogScreen::class, ResourceScreenshotPreviewDialogScreen.serializer())
                subclass(ResourceTextSelectionDialogScreen::class, ResourceTextSelectionDialogScreen.serializer())
                subclass(ResourceActionsBottomSheetScreen::class, ResourceActionsBottomSheetScreen.serializer())
                subclass(AboutAppScreen::class, AboutAppScreen.serializer())
                subclass(ResourceVotesBottomSheetScreen::class, ResourceVotesBottomSheetScreen.serializer())
                subclass(LinkDetailsScreen::class, LinkDetailsScreen.serializer())
                subclass(ImageViewerScreen::class, ImageViewerScreen.serializer())
                subclass(TagScreen::class, TagScreen.serializer())
                subclass(EntryDetailsScreen::class, EntryDetailsScreen.serializer())
                subclass(SettingsScreen::class, SettingsScreen.serializer())
                subclass(HitsScreen::class, HitsScreen.serializer())
                subclass(SearchScreen::class, SearchScreen.serializer())
                subclass(AdvancedSearchScreen::class, AdvancedSearchScreen.serializer())
                subclass(ConversationScreen::class, ConversationScreen.serializer())
                subclass(PrivateMessagesScreen::class, PrivateMessagesScreen.serializer())
                subclass(NewConversationScreen::class, NewConversationScreen.serializer())
                subclass(ComposerBottomSheetScreen::class, ComposerBottomSheetScreen.serializer())
            }
        }
    }

    fun serialize(backStack: List<NavTarget>): String? = runCatching {
        json.encodeToString(backStackSerializer, backStack)
    }.getOrNull()

    fun deserialize(payload: String): List<NavTarget>? = runCatching {
        json.decodeFromString(backStackSerializer, payload)
            .takeIf(List<NavTarget>::isNotEmpty)
    }.getOrNull()
}
