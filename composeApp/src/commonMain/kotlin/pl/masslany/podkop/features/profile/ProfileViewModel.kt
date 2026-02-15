package pl.masslany.podkop.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.topbar.TopBarActions

class ProfileViewModel(
    private val username: String?,
    private val configStorage: ConfigStorage,
    private val authRepository: AuthRepository,
    private val appNavigator: AppNavigator,
    topBarActions: TopBarActions,
) : ViewModel(),
    ProfileActions,
    TopBarActions by topBarActions {

    private val _state = MutableStateFlow(ProfileScreenState.initial)
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = ProfileScreenState.initial,
        )

    init {
        viewModelScope.launch {
            val content = resolveContentState()
            _state.update {
                it.copy(
                    isLoading = false,
                    content = content,
                )
            }
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

    private suspend fun resolveContentState(): ProfileContentState {
        username?.takeIf { it.isNotBlank() }?.let {
            return ProfileContentState.User(username = it)
        }

        val refreshToken = configStorage.getRefreshToken()
        return if (refreshToken.isBlank()) {
            ProfileContentState.LoggedOut
        } else {
            ProfileContentState.CurrentUser
        }
    }
}
