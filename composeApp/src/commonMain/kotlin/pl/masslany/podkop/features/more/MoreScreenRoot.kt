package pl.masslany.podkop.features.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.more.components.MoreHeader
import pl.masslany.podkop.features.more.components.MoreSectionCard
import pl.masslany.podkop.features.more.models.MoreSectionItemState
import pl.masslany.podkop.features.more.models.MoreSectionItemType
import pl.masslany.podkop.features.more.models.MoreSectionState
import pl.masslany.podkop.features.more.models.MoreSectionType
import pl.masslany.podkop.features.more.preview.NoOpMoreActions
import pl.masslany.podkop.features.profile.models.MemberSinceState
import pl.masslany.podkop.features.profile.models.ProfileHeaderState

@Composable
internal fun MoreScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<MoreViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    MoreScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
    )
}

@Composable
fun MoreScreenContent(
    paddingValues: PaddingValues,
    state: MoreScreenState,
    actions: MoreActions,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                ),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 12.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "more_header") {
                MoreHeader(
                    state = state,
                    onProfileClicked = actions::onProfileClicked,
                    onLoginClicked = actions::onLoginClicked,
                )
            }

            items(
                items = state.sections,
                key = { section -> section.type.name },
            ) { section ->
                MoreSectionCard(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                        ),
                    section = section,
                    actions = actions,
                )
            }
        }
    }
}

@Preview(name = "More Logged Out", widthDp = 390, heightDp = 844)
@Composable
private fun MoreScreenContentLoggedOutPreview() {
    PodkopPreview(darkTheme = false) {
        MoreScreenContent(
            paddingValues = PaddingValues(),
            state = MoreScreenState(
                isLoading = false,
                isLoggedIn = false,
                profileHeader = null,
                sections = persistentListOf(
                    MoreSectionState(
                        type = MoreSectionType.Social,
                        items = persistentListOf(
                            MoreSectionItemState(
                                type = MoreSectionItemType.Notifications,
                                badgeCount = 2,
                            ),
                        ),
                    ),
                ),
            ),
            actions = NoOpMoreActions,
        )
    }
}

@Preview(name = "More Logged In", widthDp = 390, heightDp = 844)
@Composable
private fun MoreScreenContentLoggedInPreview() {
    PodkopPreview(darkTheme = false) {
        MoreScreenContent(
            paddingValues = PaddingValues(),
            state = MoreScreenState(
                isLoading = false,
                isLoggedIn = true,
                profileHeader = ProfileHeaderState(
                    username = "patryk",
                    avatarUrl = "https://picsum.photos/seed/profile-avatar/160/160",
                    backgroundUrl = "https://picsum.photos/seed/profile-bg/1200/600",
                    genderIndicatorType = GenderIndicatorType.Male,
                    nameColorType = NameColorType.Orange,
                    memberSinceState = MemberSinceState.YearsAndMonths(
                        years = 6,
                        months = 2,
                    ),
                ),
                sections = persistentListOf(
                    MoreSectionState(
                        type = MoreSectionType.System,
                        items = persistentListOf(
                            MoreSectionItemState(
                                type = MoreSectionItemType.Settings,
                                badgeCount = 0,
                            ),
                            MoreSectionItemState(
                                type = MoreSectionItemType.About,
                                badgeCount = 0,
                            ),
                        ),
                    ),
                ),
            ),
            actions = NoOpMoreActions,
        )
    }
}
