package pl.masslany.podkop.features.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.deeplink.AuthSessionEvent
import pl.masslany.podkop.common.deeplink.AuthSessionEvents
import pl.masslany.podkop.common.extensions.toMemberSinceState
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.platform.BuildInfo
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.about.AboutAppScreen
import pl.masslany.podkop.features.favorites.FavoritesScreen
import pl.masslany.podkop.features.hits.HitsScreen
import pl.masslany.podkop.features.more.models.MoreSectionItemState
import pl.masslany.podkop.features.more.models.MoreSectionItemType
import pl.masslany.podkop.features.more.models.MoreSectionState
import pl.masslany.podkop.features.more.models.MoreSectionType
import pl.masslany.podkop.features.notifications.NotificationsScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import pl.masslany.podkop.features.search.SearchScreen
import pl.masslany.podkop.features.settings.SettingsScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.more_snackbar_coming_soon

class MoreViewModel(
    private val authRepository: AuthRepository,
    private val authSessionEvents: AuthSessionEvents,
    private val notificationsRepository: NotificationsRepository,
    private val profileRepository: ProfileRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val buildInfo: BuildInfo,
) : ViewModel(),
    MoreActions {
    private val _state = MutableStateFlow(MoreScreenState.initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
        observeAuthSessionChanges()
        observeNotificationsUnreadCount()
    }

    fun onScreenOpened() {
        viewModelScope.launch {
            notificationsRepository.refreshStatus()
        }
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
        appNavigator.navigateTo(NotificationsScreen)
    }

    override fun onMessagesClicked() {
        emitComingSoon()
    }

    override fun onFavoritesClicked() {
        appNavigator.navigateTo(FavoritesScreen)
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
        appNavigator.navigateTo(AboutAppScreen)
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

    private fun observeNotificationsUnreadCount() {
        viewModelScope.launch {
            notificationsRepository.unreadCount.collectLatest { unreadCount ->
                _state.update { currentState ->
                    currentState.copy(
                        sections = buildMoreSections(
                            notificationsUnreadCount = unreadCount,
                            isLoggedIn = currentState.isLoggedIn,
                        ),
                    )
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
                    notificationsUnreadCount = notificationsRepository.unreadCount.value,
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
                        notificationsUnreadCount = notificationsRepository.unreadCount.value,
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
                        notificationsUnreadCount = notificationsRepository.unreadCount.value,
                        isLoggedIn = true,
                    ),
                )
            }
    }

    private fun buildMoreSections(
        notificationsUnreadCount: Int? = null,
        isLoggedIn: Boolean,
    ): ImmutableList<MoreSectionState> = persistentListOf(
        MoreSectionState(
            type = MoreSectionType.Social,
            items = buildList {
                if (isLoggedIn) {
                    add(
                        MoreSectionItemState(
                            type = MoreSectionItemType.Notifications,
                            badgeCount = notificationsUnreadCount,
                        ),
                    )
                }
                if (buildInfo.isDebugBuild && isLoggedIn) {
                    add(
                        MoreSectionItemState(
                            type = MoreSectionItemType.Messages,
                            badgeCount = null,
                        ),
                    )
                }
                if (isLoggedIn) {
                    add(
                        MoreSectionItemState(
                            type = MoreSectionItemType.Favorites,
                            badgeCount = null,
                        ),
                    )
                }
            }.toPersistentList(),
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
                    badgeCount = null,
                ),
                MoreSectionItemState(
                    type = MoreSectionItemType.About,
                    badgeCount = null,
                ),
            ),
        ),
    )

    private fun buildContentSectionItems(
        isLoggedIn: Boolean,

    ): ImmutableList<MoreSectionItemState> = buildList {
        add(
            MoreSectionItemState(
                type = MoreSectionItemType.Hits,
                badgeCount = null,
            ),
        )
        add(
            MoreSectionItemState(
                type = MoreSectionItemType.Search,
                badgeCount = null,
            ),
        )
        if (isLoggedIn && buildInfo.isDebugBuild) {
            add(
                MoreSectionItemState(
                    type = MoreSectionItemType.MyWykop,
                    badgeCount = null,
                ),
            )
        }
    }.toPersistentList()

    private fun emitComingSoon() {
        snackbarManager.tryEmit(
            SnackbarEvent(
                message = SnackbarMessage.Resource(Res.string.more_snackbar_coming_soon),
                isFinite = true,
            ),
        )
    }
}
