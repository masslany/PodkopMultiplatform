package pl.masslany.podkop.common.models.avatar

sealed class AvatarType {
    data class NetworkImage(val url: String) : AvatarType()
    data object NoAvatar : AvatarType()
}
