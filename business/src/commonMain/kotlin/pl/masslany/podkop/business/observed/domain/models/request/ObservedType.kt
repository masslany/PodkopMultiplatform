package pl.masslany.podkop.business.observed.domain.models.request

enum class ObservedType(val endpointPath: String) {
    All("all"),
    Profiles("users"),
    Discussions("discussions"),
    Tags("tags/stream"),
}
