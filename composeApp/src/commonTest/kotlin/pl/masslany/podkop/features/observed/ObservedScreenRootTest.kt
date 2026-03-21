package pl.masslany.podkop.features.observed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.observed_empty_discussions
import podkop.composeapp.generated.resources.observed_empty_everything
import podkop.composeapp.generated.resources.observed_empty_profiles
import podkop.composeapp.generated.resources.observed_empty_tags

class ObservedScreenRootTest {
    @Test
    fun `discussion banner is shown only when new content count is positive`() {
        assertNull(Resource.Entry.toObservedDiscussionBannerState(newContentCount = 0))
        assertNull(Resource.Link.toObservedDiscussionBannerState(newContentCount = null))
        assertNull(Resource.EntryComment.toObservedDiscussionBannerState(newContentCount = 3))
        assertEquals(
            ObservedDiscussionBannerState(
                newContentCount = 3,
                type = ObservedDiscussionBannerType.Entry,
            ),
            Resource.Entry.toObservedDiscussionBannerState(newContentCount = 3),
        )
    }

    @Test
    fun `empty state copy matches all filter types`() {
        assertEquals(Res.string.observed_empty_everything, observedEmptyMessageRes(ObservedType.All))
        assertEquals(Res.string.observed_empty_profiles, observedEmptyMessageRes(ObservedType.Profiles))
        assertEquals(
            Res.string.observed_empty_discussions,
            observedEmptyMessageRes(ObservedType.Discussions),
        )
        assertEquals(Res.string.observed_empty_tags, observedEmptyMessageRes(ObservedType.Tags))
    }
}
