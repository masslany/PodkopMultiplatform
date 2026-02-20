package pl.masslany.podkop.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.ZERO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.Summary
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.settings.SettingsScreen
import pl.masslany.podkop.features.topbar.TopBarActions

class ProfileViewModel(
    private val username: String?,
    private val configStorage: ConfigStorage,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val appNavigator: AppNavigator,
    topBarActions: TopBarActions,
) : ViewModel(),
    ProfileActions,
    TopBarActions by topBarActions,
    ResourceItemActions by resourceItemStateHolder {

    private val _state = MutableStateFlow(ProfileScreenState.initial)
    val state = combine(_state, resourceItemStateHolder.items) { state, resources ->
        state.copy(resources = resources)
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = ProfileScreenState.initial,
        )

    init {
        resourceItemStateHolder.init(viewModelScope)
        viewModelScope.launch {
            loadData()
        }
    }

    override fun onLoginClicked() {
        viewModelScope.launch {
            authRepository.getWykopConnect()
                .onSuccess { connectUrl ->
                    appNavigator.openExternalLink(connectUrl)
                }
                .onFailure {
                    println("DBG --> failed to resolve wykop connect url: $it")
                }
        }
    }

    override fun onTopBarSettingsClicked() {
        appNavigator.navigateTo(SettingsScreen)
    }

    override fun onRetryClicked() {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        _state.update { previousState ->
            previousState.copy(
                isLoading = true,
            )
        }

        val requestedUsername = username?.trim()?.takeIf { it.isNotEmpty() }
        val isCurrentUserProfile = requestedUsername == null

        if (isCurrentUserProfile && configStorage.getRefreshToken().isBlank()) {
            resourceItemStateHolder.updateData(emptyList())
            _state.update { previousState ->
                previousState.copy(
                    isLoading = false,
                    content = ProfileContentState.LoggedOut,
                )
            }
            return
        }

        val profileResult = if (isCurrentUserProfile) {
            profileRepository.getProfile()
        } else {
            profileRepository.getProfile(requestedUsername)
        }

        profileResult.onSuccess { profile ->
            val actionsUsername = requestedUsername ?: profile.name
            profileRepository.getProfileActions(
                username = actionsUsername,
                page = 1,
            ).onSuccess {
                resourceItemStateHolder.updateData(it.data)
            }.onFailure {
                resourceItemStateHolder.updateData(emptyList())
                println("DBG --> failed to load profile actions with $it")
            }

            _state.update { previousState ->
                previousState.copy(
                    isLoading = false,
                    content = ProfileContentState.Loaded(
                        isCurrentUser = isCurrentUserProfile,
                        header = profile.toProfileHeaderState(),
                        summary = profile.summary.toSummaryItems(),
                    ),
                )
            }
        }.onFailure {
            resourceItemStateHolder.updateData(emptyList())
            _state.update { previousState ->
                previousState.copy(
                    isLoading = false,
                    content = ProfileContentState.Error,
                )
            }
            println("DBG --> failed to load profile with $it")
        }
    }
}

private fun Profile.toProfileHeaderState(): ProfileHeaderState =
    ProfileHeaderState(
        username = name,
        avatarUrl = avatarUrl,
        backgroundUrl = backgroundUrl,
        genderIndicatorType = gender.toGenderIndicatorType(),
        nameColorType = color.toNameColorType(),
        memberSinceState = memberSince.toMemberSinceState(),
    )

private fun Summary.toSummaryItems(): ImmutableList<ProfileSummaryItem> =
    persistentListOf(
        ProfileSummaryItem.Actions(actions),
        ProfileSummaryItem.Links(links),
        ProfileSummaryItem.Entries(entries),
        ProfileSummaryItem.Followers(followers),
        ProfileSummaryItem.FollowingTags(followingTags),
        ProfileSummaryItem.FollowingUsers(followingUsers),
    )

private fun LocalDateTime?.toMemberSinceState(): MemberSinceState {
    if (this == null) {
        return MemberSinceState.Unknown
    }

    val timeZone = TimeZone.currentSystemDefault()
    var membershipDuration = Clock.System.now() - this.toInstant(timeZone)
    if (membershipDuration < ZERO) {
        membershipDuration = ZERO
    }

    val days = membershipDuration.inWholeDays.toInt()

    return when {
        days >= DAYS_IN_YEAR -> {
            val years = days / DAYS_IN_YEAR
            val months = (days % DAYS_IN_YEAR) / DAYS_IN_MONTH
            if (months == 0) {
                MemberSinceState.Years(years)
            } else {
                MemberSinceState.YearsAndMonths(
                    years = years,
                    months = months,
                )
            }
        }

        days >= DAYS_IN_MONTH -> {
            MemberSinceState.Months(days / DAYS_IN_MONTH)
        }

        else -> {
            MemberSinceState.Days(days)
        }
    }
}

private const val DAYS_IN_MONTH = 30
private const val DAYS_IN_YEAR = 365
