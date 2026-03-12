package pl.masslany.podkop.features.linksubmission.linkdraft.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.linksubmission.linkdraft.LinkDraftScreenState
import pl.masslany.podkop.features.linksubmission.models.AddLinkSuggestedImageState
import pl.masslany.podkop.features.linksubmission.models.AddLinkTagSuggestionState

class LinkDraftScreenStateProvider : PreviewParameterProvider<LinkDraftScreenState> {
    override val values: Sequence<LinkDraftScreenState> = sequenceOf(
        LinkDraftScreenState.initial.copy(
            draftKey = "draft-loading",
            isLoadingDraft = true,
        ),
        LinkDraftScreenState.initial.copy(
            currentUrl = "https://www.pcmag.com/news/asus-co-ceo-macbook-neo-is-a-shock-to-the-pc-industry",
            draftKey = "draft-3",
            title = "Asus Co",
            description = "Windows PC makers typically haven't had to compete with Apple on the low end.",
            tags = persistentListOf("technologia", "laptopy"),
            tagInput = "szcz",
            tagSuggestions = persistentListOf(
                AddLinkTagSuggestionState(name = "szczecin", observedQuantity = 4481),
                AddLinkTagSuggestionState(name = "szczepienia", observedQuantity = 175),
            ),
            suggestedImages = persistentListOf(
                AddLinkSuggestedImageState(url = "https://i.pcmag.com/imagery/articles/05PK323rI5BMn61kbJJdDOy-1.fit_lim.v1773154806.png"),
                AddLinkSuggestedImageState(url = "https://i.pcmag.com/imagery/articles/05PK323rI5BMn61kbJJdDOy-1.fit_lim.size_1200x630.v1773154806.png"),
            ),
            selectedSuggestedImageIndex = 0,
            adult = false,
            photoUrl = "https://wykop.pl/cdn/c3397993/889fbb7dc3d08b07b75a2ae4c43ec69e74fdd1c3e4d686c5ef61cd82365a47ad.jpg",
        ),
    )
}
