package pl.masslany.podkop.features.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.deeplink.AuthSessionEvent
import pl.masslany.podkop.common.deeplink.AuthSessionEvents
import pl.masslany.podkop.common.extensions.toMemberSinceState
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.hits.HitsScreen
import pl.masslany.podkop.features.more.models.MoreSectionItemState
import pl.masslany.podkop.features.more.models.MoreSectionItemType
import pl.masslany.podkop.features.more.models.MoreSectionState
import pl.masslany.podkop.features.more.models.MoreSectionType
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import pl.masslany.podkop.features.search.SearchScreen
import pl.masslany.podkop.features.settings.SettingsScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.more_about_body
import podkop.composeapp.generated.resources.more_about_title
import podkop.composeapp.generated.resources.more_snackbar_coming_soon

class MoreViewModel(
    private val authRepository: AuthRepository,
    private val authSessionEvents: AuthSessionEvents,
    private val profileRepository: ProfileRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    MoreActions {
    private val _state = MutableStateFlow(MoreScreenState.initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
        observeAuthSessionChanges()
    }

    override fun onProfileClicked() {
        val username = _state.value.profileHeader?.username ?: return
        appNavigator.navigateTo(ProfileScreen(username = username))
    }

    override fun onLoginClicked() {
        viewModelScope.launch {
            authRepository.getWykopConnect()
                .onSuccess { connectUrl ->
                    appNavigator.openExternalLink(connectUrl)
                }
                .onFailure {
                    logger.error("Failed to resolve wykop connect url in More screen", it)
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    override fun onNotificationsClicked() {
        emitComingSoon()
    }

    override fun onMessagesClicked() {
        emitComingSoon()
    }

    override fun onFavoritesClicked() {
        emitComingSoon()
    }

    override fun onHitsClicked() {
        appNavigator.navigateTo(HitsScreen)
    }

    override fun onSearchClicked() {
        appNavigator.navigateTo(SearchScreen)
    }

    override fun onMyWykopClicked() {
        emitComingSoon()
    }

    override fun onSettingsClicked() {
        appNavigator.navigateTo(SettingsScreen)
    }

    override fun onAboutClicked() {
        appNavigator.navigateTo(
            GenericDialog.fromResources(
                title = Res.string.more_about_title,
                description = Res.string.more_about_body,
            ),
        )
    }

    private fun observeAuthSessionChanges() {
        viewModelScope.launch {
            authSessionEvents.events.collect { event ->
                when (event) {
                    AuthSessionEvent.TokensUpdated -> loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        _state.update { previous ->
            previous.copy(isLoading = true)
        }

        val isLoggedIn = authRepository.isLoggedIn()
        if (!isLoggedIn) {
            _state.value = MoreScreenState(
                isLoading = false,
                isLoggedIn = false,
                profileHeader = null,
                sections = buildMoreSections(
                    notificationsUnreadCount = 0,
                    isLoggedIn = false,
                ),
            )
            return
        }

        profileRepository.getProfile()
            .onSuccess { profile ->
                _state.value = MoreScreenState(
                    isLoading = false,
                    isLoggedIn = true,
                    profileHeader = ProfileHeaderState(
                        username = profile.name,
                        avatarUrl = profile.avatarUrl,
                        backgroundUrl = profile.backgroundUrl,
                        genderIndicatorType = profile.gender.toGenderIndicatorType(),
                        nameColorType = profile.color.toNameColorType(),
                        memberSinceState = profile.memberSince.toMemberSinceState(),
                    ),
                    sections = buildMoreSections(
                        notificationsUnreadCount = 0,
                        isLoggedIn = true,
                    ),
                )
            }
            .onFailure {
                logger.error("Failed to load profile preview in More screen", it)
                _state.value = MoreScreenState(
                    isLoading = false,
                    isLoggedIn = true,
                    profileHeader = null,
                    sections = buildMoreSections(
                        notificationsUnreadCount = 0,
                        isLoggedIn = true,
                    ),
                )
            }
    }

    private fun buildMoreSections(
        notificationsUnreadCount: Int,
        isLoggedIn: Boolean,
    ): ImmutableList<MoreSectionState> = persistentListOf(
        MoreSectionState(
            type = MoreSectionType.Social,
            items = persistentListOf(
                MoreSectionItemState(
                    type = MoreSectionItemType.Notifications,
                    badgeCount = notificationsUnreadCount,
                    onClick = { onNotificationsClicked() },
                ),
                MoreSectionItemState(
                    type = MoreSectionItemType.Messages,
                    badgeCount = 0,
                    onClick = { onMessagesClicked() },
                ),
                MoreSectionItemState(
                    type = MoreSectionItemType.Favorites,
                    badgeCount = 0,
                    onClick = { onFavoritesClicked() },
                ),
            ),
        ),
        MoreSectionState(
            type = MoreSectionType.Content,
            items = buildContentSectionItems(isLoggedIn = isLoggedIn),
        ),
        MoreSectionState(
            type = MoreSectionType.System,
            items = persistentListOf(
                MoreSectionItemState(
                    type = MoreSectionItemType.Settings,
                    badgeCount = 0,
                    onClick = { onSettingsClicked() },
                ),
                MoreSectionItemState(
                    type = MoreSectionItemType.About,
                    badgeCount = 0,
                    onClick = { onAboutClicked() },
                ),
            ),
        ),
    )

    private fun buildContentSectionItems(isLoggedIn: Boolean): ImmutableList<MoreSectionItemState> {
        val baseItems = mutableListOf(
            MoreSectionItemState(
                type = MoreSectionItemType.Hits,
                badgeCount = 0,
                onClick = { onHitsClicked() },
            ),
            MoreSectionItemState(
                type = MoreSectionItemType.Search,
                badgeCount = 0,
                onClick = { onSearchClicked() },
            ),
        )

        if (isLoggedIn) {
            baseItems += MoreSectionItemState(
                type = MoreSectionItemType.MyWykop,
                badgeCount = 0,
                onClick = { onMyWykopClicked() },
            )
        }

        return baseItems.toPersistentList()
    }

    private fun emitComingSoon() {
        snackbarManager.tryEmit(
            SnackbarEvent(
                message = SnackbarMessage.Resource(Res.string.more_snackbar_coming_soon),
                isFinite = true,
            ),
        )
    }
}
