package pl.masslany.podkop.features.search.preview

import pl.masslany.podkop.features.search.SearchActions

object NoOpSearchActions : SearchActions {
    override fun onQueryChanged(value: String) = Unit

    override fun onTagClicked(tag: String) = Unit

    override fun onUserClicked(username: String) = Unit

    override fun onRetryClicked() = Unit

    override fun onTopBarBackClicked() = Unit
}
