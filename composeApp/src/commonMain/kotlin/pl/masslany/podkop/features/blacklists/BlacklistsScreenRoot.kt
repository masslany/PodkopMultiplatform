package pl.masslany.podkop.features.blacklists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.SectionCardDivider
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.extensions.rememberWindowSizeClass
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.pagination.rememberLazyGridPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.blacklists.components.BlacklistAddForm
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryState
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState
import pl.masslany.podkop.features.blacklists.preview.BlacklistsScreenStateProvider
import pl.masslany.podkop.features.blacklists.preview.NoOpBlacklistsActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_blacklists_remove
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.blacklists_empty_domains
import podkop.composeapp.generated.resources.blacklists_empty_tags
import podkop.composeapp.generated.resources.blacklists_empty_users
import podkop.composeapp.generated.resources.blacklists_intro
import podkop.composeapp.generated.resources.blacklists_tab_domains
import podkop.composeapp.generated.resources.blacklists_tab_tags
import podkop.composeapp.generated.resources.blacklists_tab_users
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_delete
import podkop.composeapp.generated.resources.topbar_label_blacklists

private val ContentMaxWidth = 1080.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BlacklistsScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<BlacklistsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyGridState = rememberLazyGridPaginator(
        resetStateKey = state.selectedCategory.name,
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = viewModel::paginate,
    )

    BlacklistsScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyGridState = lazyGridState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistsScreenContent(
    paddingValues: PaddingValues,
    state: BlacklistsScreenState,
    actions: BlacklistsActions,
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
) {
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false, includeBottom = false)
    val windowSizeClass = rememberWindowSizeClass()
    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_blacklists))
                },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_back),
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
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = actions::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues),
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                state = lazyGridState,
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = WindowInsets
                        .navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 24.dp,
                ),
            ) {
                item(
                    key = "blacklists-info",
                    span = FullLineSpan,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = ContentMaxWidth),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                            ),
                        ) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = stringResource(resource = Res.string.blacklists_intro),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                item(
                    key = "blacklists-tabs",
                    span = FullLineSpan,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        BlacklistsTabs(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = ContentMaxWidth),
                            categories = state.categories,
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = actions::onCategorySelected,
                        )
                    }
                }

                item(
                    key = "blacklists-add-form",
                    span = FullLineSpan,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        BlacklistAddForm(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = ContentMaxWidth),
                            state = state.selectedCategoryState,
                            onInputChanged = actions::onAddInputChanged,
                            onAddClicked = actions::onAddClicked,
                            onSuggestionClicked = actions::onSuggestionClicked,
                            onRetrySuggestionsClicked = actions::onRetrySuggestionsClicked,
                        )
                    }
                }

                when {
                    state.selectedCategoryState.isLoading -> {
                        item(
                            key = "blacklists-loading",
                            span = FullLineSpan,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    state.selectedCategoryState.isError -> {
                        item(
                            key = "blacklists-error",
                            span = FullLineSpan,
                        ) {
                            GenericErrorScreen(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                onRefreshClicked = actions::onRefresh,
                            )
                        }
                    }

                    state.selectedCategoryState.items.isEmpty() -> {
                        item(
                            key = "blacklists-empty",
                            span = FullLineSpan,
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                text = when (state.selectedCategory) {
                                    BlacklistCategoryType.Users -> {
                                        stringResource(resource = Res.string.blacklists_empty_users)
                                    }

                                    BlacklistCategoryType.Tags -> {
                                        stringResource(resource = Res.string.blacklists_empty_tags)
                                    }

                                    BlacklistCategoryType.Domains -> {
                                        stringResource(resource = Res.string.blacklists_empty_domains)
                                    }
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    else -> {
                        items(
                            count = state.selectedCategoryState.items.size,
                            key = { index -> state.selectedCategoryState.items[index].key },
                        ) { index ->
                            BlacklistEntryCard(
                                item = state.selectedCategoryState.items[index],
                                isActionInProgress = state.selectedCategoryState.isActionsInProgress,
                                onEntryClicked = actions::onEntryClicked,
                                onRemoveClicked = actions::onRemoveClicked,
                            )
                        }

                        if (state.selectedCategoryState.isPaginating) {
                            item(
                                key = "blacklists-pagination",
                                span = FullLineSpan,
                            ) {
                                PaginationLoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlacklistsTabs(
    categories: ImmutableList<BlacklistCategoryState>,
    selectedCategory: BlacklistCategoryType,
    onCategorySelected: (BlacklistCategoryType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            categories.forEach { categoryState ->
                val selected = categoryState.type == selectedCategory
                Column(
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onCategorySelected(categoryState.type) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "${categoryState.type.toDisplayLabel()} (${categoryState.totalCount})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Medium
                        },
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(72.dp)
                            .background(
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                },
                                shape = MaterialTheme.shapes.small,
                            ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SectionCardDivider()
    }
}

@Composable
private fun BlacklistEntryCard(
    item: BlacklistEntryState,
    isActionInProgress: Boolean,
    onEntryClicked: (BlacklistEntryState) -> Unit,
    onRemoveClicked: (BlacklistEntryState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isNavigable =
        item is BlacklistEntryState.BlacklistedUserItemState || item is BlacklistEntryState.BlacklistedTagItemState

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isNavigable && !isActionInProgress) {
                        Modifier.clickable { onEntryClicked(item) }
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (item) {
                is BlacklistEntryState.BlacklistedUserItemState -> {
                    Avatar(
                        state = item.avatarState,
                        onClick = {
                            if (!isActionInProgress) {
                                onEntryClicked(item)
                            }
                        },
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = item.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = item.nameColorType.toComposeColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                is BlacklistEntryState.BlacklistedTagItemState -> {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = item.displayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                is BlacklistEntryState.BlacklistedDomainItemState -> {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = item.domain,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(
                enabled = !isActionInProgress,
                onClick = { onRemoveClicked(item) },
            ) {
                Icon(
                    imageVector = vectorResource(resource = Res.drawable.ic_delete),
                    contentDescription = stringResource(
                        resource = Res.string.accessibility_blacklists_remove,
                        item.displayLabel,
                    ),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun BlacklistCategoryType.toDisplayLabel(): String = when (this) {
    BlacklistCategoryType.Users -> stringResource(resource = Res.string.blacklists_tab_users)
    BlacklistCategoryType.Tags -> stringResource(resource = Res.string.blacklists_tab_tags)
    BlacklistCategoryType.Domains -> stringResource(resource = Res.string.blacklists_tab_domains)
}

private val FullLineSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

@Preview
@Composable
private fun BlacklistsScreenContentPreview(
    @PreviewParameter(BlacklistsScreenStateProvider::class) state: BlacklistsScreenState,
) {
    PodkopPreview(darkTheme = false) {
        BlacklistsScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpBlacklistsActions,
            lazyGridState = rememberLazyGridState(),
        )
    }
}
