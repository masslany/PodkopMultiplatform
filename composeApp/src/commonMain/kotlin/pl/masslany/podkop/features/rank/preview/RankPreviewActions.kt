package pl.masslany.podkop.features.rank.preview

import pl.masslany.podkop.features.rank.RankActions

object RankPreviewActions : RankActions {
    override fun onRefresh() = Unit

    override fun onUserClicked(username: String) = Unit

    override fun onTopBarBackClicked() = Unit

    override fun onTopBarSearchClicked() = Unit

    override fun onTopBarNotificationsClicked() = Unit

    override fun onTopBarAddEntryClicked() = Unit

    override fun onTopBarAddLinkClicked() = Unit
}
