package pl.masslany.podkop.business.startup.models

sealed class AppState {
    data object Initializing : AppState()

    data object Ready : AppState()

    data object Error : AppState()
}
