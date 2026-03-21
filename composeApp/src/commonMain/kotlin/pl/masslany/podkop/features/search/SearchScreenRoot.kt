package pl.masslany.podkop.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette
import pl.masslany.podkop.features.search.preview.NoOpSearchActions
import pl.masslany.podkop.features.search.preview.SearchScreenStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_close
import podkop.composeapp.generated.resources.ic_search
import podkop.composeapp.generated.resources.refresh_button
import podkop.composeapp.generated.resources.search_screen_advanced_search_button
import podkop.composeapp.generated.resources.search_screen_empty_prompt
import podkop.composeapp.generated.resources.search_screen_min_query_hint
import podkop.composeapp.generated.resources.search_screen_no_results
import podkop.composeapp.generated.resources.search_screen_tags_followers
import podkop.composeapp.generated.resources.search_screen_tags_result_failure
import podkop.composeapp.generated.resources.search_screen_tags_result_label
import podkop.composeapp.generated.resources.search_screen_text_field_placeholder
import podkop.composeapp.generated.resources.search_screen_users_result_failure
import podkop.composeapp.generated.resources.search_screen_users_result_label
import podkop.composeapp.generated.resources.topbar_label_search

private val SearchFieldMaxWidth = 640.dp
private val SearchContentMaxWidth = 980.dp
private val SearchWideBreakpoint = 840.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<SearchViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SearchScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    paddingValues: PaddingValues,
    state: SearchScreenState,
    actions: SearchActions,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_search))
                },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_topbar_back,
                            ),
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                windowInsets = topBarInsets,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = contentInsets,
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = SearchFieldMaxWidth)
                    .align(Alignment.CenterHorizontally),
                value = state.query,
                onValueChange = actions::onQueryChanged,
                singleLine = true,
                placeholder = {
                    Text(text = stringResource(resource = Res.string.search_screen_text_field_placeholder))
                },
                leadingIcon = {
                    Icon(
                        imageVector = vectorResource(resource = Res.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                actions.onQueryChanged("")
                            },
                        ) {
                            Icon(
                                imageVector = vectorResource(resource = Res.drawable.ic_close),
                                contentDescription = null,
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        actions.onAdvancedSearchClicked()
                    },
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = SearchFieldMaxWidth)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = actions::onAdvancedSearchClicked) {
                    Text(text = stringResource(resource = Res.string.search_screen_advanced_search_button))
                }
            }

            SearchBody(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                state = state,
                actions = actions,
            )
        }
    }
}

@Composable
private fun SearchBody(
    state: SearchScreenState,
    actions: SearchActions,
    modifier: Modifier = Modifier,
) {
    val trimmedQuery = state.query.trim()

    when {
        trimmedQuery.isEmpty() -> {
            SearchInfoMessage(
                modifier = modifier,
                text = stringResource(resource = Res.string.search_screen_empty_prompt),
            )
        }

        trimmedQuery.length < state.minQueryLength -> {
            SearchInfoMessage(
                modifier = modifier,
                text = stringResource(
                    resource = Res.string.search_screen_min_query_hint,
                    state.minQueryLength,
                ),
            )
        }

        else -> {
            SearchResultsLayout(
                modifier = modifier,
                state = state,
                actions = actions,
            )
        }
    }
}

@Composable
private fun SearchInfoMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchResultsLayout(
    state: SearchScreenState,
    actions: SearchActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = SearchContentMaxWidth)
                .align(Alignment.TopCenter),
        ) {
            val isWideLayout = maxWidth >= SearchWideBreakpoint && state.showUserSuggestions

            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        item(key = "tags-section-wide") {
                            TagsResultsSection(
                                sectionState = state.tags,
                                onTagClicked = actions::onTagClicked,
                                onRetryClicked = actions::onRetryClicked,
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        item(key = "users-section-wide") {
                            UsersResultsSection(
                                sectionState = state.users,
                                onUserClicked = actions::onUserClicked,
                                onRetryClicked = actions::onRetryClicked,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    item(key = "tags-section") {
                        TagsResultsSection(
                            sectionState = state.tags,
                            onTagClicked = actions::onTagClicked,
                            onRetryClicked = actions::onRetryClicked,
                        )
                    }

                    if (state.showUserSuggestions) {
                        item(key = "users-section") {
                            UsersResultsSection(
                                sectionState = state.users,
                                onUserClicked = actions::onUserClicked,
                                onRetryClicked = actions::onRetryClicked,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagsResultsSection(
    sectionState: SearchSectionState<TagSuggestionItemState>,
    onTagClicked: (String) -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchSectionCard(
        modifier = modifier,
        title = stringResource(resource = Res.string.search_screen_tags_result_label),
    ) {
        when (sectionState.status) {
            SearchSectionStatus.Hidden -> Unit

            SearchSectionStatus.Loading -> SectionLoading()

            SearchSectionStatus.Error -> SectionError(
                message = stringResource(resource = Res.string.search_screen_tags_result_failure),
                onRetryClicked = onRetryClicked,
            )

            SearchSectionStatus.Content -> {
                if (sectionState.items.isEmpty()) {
                    SectionEmpty()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        sectionState.items.forEachIndexed { index, item ->
                            TagSuggestionRow(
                                state = item,
                                onClick = { onTagClicked(item.name) },
                            )
                            if (index != sectionState.items.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersResultsSection(
    sectionState: SearchSectionState<UserSuggestionItemState>,
    onUserClicked: (String) -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchSectionCard(
        modifier = modifier,
        title = stringResource(resource = Res.string.search_screen_users_result_label),
    ) {
        when (sectionState.status) {
            SearchSectionStatus.Hidden -> Unit

            SearchSectionStatus.Loading -> SectionLoading()

            SearchSectionStatus.Error -> SectionError(
                message = stringResource(resource = Res.string.search_screen_users_result_failure),
                onRetryClicked = onRetryClicked,
            )

            SearchSectionStatus.Content -> {
                if (sectionState.items.isEmpty()) {
                    SectionEmpty()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        sectionState.items.forEachIndexed { index, item ->
                            UserSuggestionRow(
                                state = item,
                                onClick = { onUserClicked(item.username) },
                            )
                            if (index != sectionState.items.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun SectionLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun SectionError(
    message: String,
    onRetryClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetryClicked) {
            Text(text = stringResource(resource = Res.string.refresh_button))
        }
    }
}

@Composable
private fun SectionEmpty() {
    Text(
        text = stringResource(resource = Res.string.search_screen_no_results),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TagSuggestionRow(
    state: TagSuggestionItemState,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "#${state.name}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorsPalette.tagBlue,
        )
        Text(
            text = stringResource(
                resource = Res.string.search_screen_tags_followers,
                state.followers,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UserSuggestionRow(
    state: UserSuggestionItemState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Avatar(
            state = AvatarState(
                type = if (state.avatarUrl.isBlank()) {
                    AvatarType.NoAvatar
                } else {
                    AvatarType.NetworkImage(state.avatarUrl)
                },
                genderIndicatorType = state.genderIndicatorType,
            ),
            onClick = onClick,
        )
        Text(
            text = state.username,
            style = MaterialTheme.typography.bodyLarge,
            color = state.nameColorType.toComposeColor(),
        )
    }
}

@Preview(name = "Search Compact", widthDp = 390, heightDp = 844)
@Composable
private fun SearchScreenContentCompactPreview(
    @PreviewParameter(SearchScreenStateProvider::class) state: SearchScreenState,
) {
    PodkopPreview(darkTheme = false) {
        SearchScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpSearchActions,
        )
    }
}

@Preview(name = "Search Wide", widthDp = 960, heightDp = 720)
@Composable
private fun SearchScreenContentWidePreview(
    @PreviewParameter(SearchScreenStateProvider::class) state: SearchScreenState,
) {
    PodkopPreview(darkTheme = false) {
        SearchScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpSearchActions,
        )
    }
}
