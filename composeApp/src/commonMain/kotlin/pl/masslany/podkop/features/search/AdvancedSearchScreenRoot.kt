package pl.masslany.podkop.features.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.business.search.domain.models.request.SearchSort
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.advanced_search_screen_category_label
import podkop.composeapp.generated.resources.advanced_search_screen_category_placeholder
import podkop.composeapp.generated.resources.advanced_search_screen_custom_date
import podkop.composeapp.generated.resources.advanced_search_screen_date_24h
import podkop.composeapp.generated.resources.advanced_search_screen_date_30d
import podkop.composeapp.generated.resources.advanced_search_screen_date_3d
import podkop.composeapp.generated.resources.advanced_search_screen_date_7d
import podkop.composeapp.generated.resources.advanced_search_screen_date_all
import podkop.composeapp.generated.resources.advanced_search_screen_date_from_label
import podkop.composeapp.generated.resources.advanced_search_screen_date_label
import podkop.composeapp.generated.resources.advanced_search_screen_date_placeholder
import podkop.composeapp.generated.resources.advanced_search_screen_date_to_label
import podkop.composeapp.generated.resources.advanced_search_screen_date_year
import podkop.composeapp.generated.resources.advanced_search_screen_domains_label
import podkop.composeapp.generated.resources.advanced_search_screen_empty_results
import podkop.composeapp.generated.resources.advanced_search_screen_error
import podkop.composeapp.generated.resources.advanced_search_screen_filters_title
import podkop.composeapp.generated.resources.advanced_search_screen_idle_message
import podkop.composeapp.generated.resources.advanced_search_screen_invalid_date_format
import podkop.composeapp.generated.resources.advanced_search_screen_invalid_date_range
import podkop.composeapp.generated.resources.advanced_search_screen_multi_value_hint
import podkop.composeapp.generated.resources.advanced_search_screen_query_label
import podkop.composeapp.generated.resources.advanced_search_screen_query_placeholder
import podkop.composeapp.generated.resources.advanced_search_screen_query_required
import podkop.composeapp.generated.resources.advanced_search_screen_search_button
import podkop.composeapp.generated.resources.advanced_search_screen_sort_comments
import podkop.composeapp.generated.resources.advanced_search_screen_sort_label
import podkop.composeapp.generated.resources.advanced_search_screen_sort_newest
import podkop.composeapp.generated.resources.advanced_search_screen_sort_popular
import podkop.composeapp.generated.resources.advanced_search_screen_sort_score
import podkop.composeapp.generated.resources.advanced_search_screen_tags_label
import podkop.composeapp.generated.resources.advanced_search_screen_users_label
import podkop.composeapp.generated.resources.advanced_search_screen_votes_all
import podkop.composeapp.generated.resources.advanced_search_screen_votes_label
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_close
import podkop.composeapp.generated.resources.ic_search
import podkop.composeapp.generated.resources.refresh_button
import podkop.composeapp.generated.resources.topbar_label_advanced_search

private val AdvancedSearchContentMaxWidth = 980.dp
private val AdvancedSearchWideFieldBreakpoint = 720.dp
private val AdvancedSearchVoteOptions = listOf<Int?>(null, 50, 100, 500, 1000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdvancedSearchScreenRoot(
    screen: AdvancedSearchScreen,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<AdvancedSearchViewModel>(
        parameters = { parametersOf(screen) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    AdvancedSearchScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdvancedSearchScreenContent(
    paddingValues: PaddingValues,
    state: AdvancedSearchScreenState,
    actions: AdvancedSearchActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false, includeBottom = false)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_advanced_search))
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
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = actions::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets
                        .navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 16.dp,
                ),
            ) {
                item(key = "advanced-search-filters") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        AdvancedSearchFiltersCard(
                            modifier = Modifier.widthIn(max = AdvancedSearchContentMaxWidth),
                            state = state,
                            actions = actions,
                            onSearchImeAction = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                actions.onSearchClicked()
                            },
                            onClearQueryClicked = {
                                actions.onQueryChanged("")
                            },
                        )
                    }
                }

                when {
                    state.isLoading -> {
                        item(key = "advanced-search-loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    state.isError -> {
                        item(key = "advanced-search-error") {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                AdvancedSearchMessagePanel(
                                    modifier = Modifier.widthIn(max = AdvancedSearchContentMaxWidth),
                                    title = stringResource(resource = Res.string.advanced_search_screen_error),
                                    actionLabel = stringResource(resource = Res.string.refresh_button),
                                    onActionClicked = actions::onRefresh,
                                )
                            }
                        }
                    }

                    !state.hasSearched -> {
                        item(key = "advanced-search-idle") {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                AdvancedSearchMessagePanel(
                                    modifier = Modifier.widthIn(max = AdvancedSearchContentMaxWidth),
                                    title = stringResource(resource = Res.string.advanced_search_screen_idle_message),
                                )
                            }
                        }
                    }

                    state.results.isEmpty() -> {
                        item(key = "advanced-search-empty") {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                AdvancedSearchMessagePanel(
                                    modifier = Modifier.widthIn(max = AdvancedSearchContentMaxWidth),
                                    title = stringResource(resource = Res.string.advanced_search_screen_empty_results),
                                )
                            }
                        }
                    }

                    else -> {
                        items(
                            items = state.results,
                            key = { item -> "${item.contentType}:${item.id}" },
                            contentType = { item -> item.contentType },
                        ) { item ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                ResourceItemRenderer(
                                    modifier = Modifier.widthIn(max = AdvancedSearchContentMaxWidth),
                                    state = item,
                                    actions = actions,
                                    config = ResourceItemConfig(
                                        showEntryInlineActions = false,
                                    ),
                                )
                            }
                        }

                        if (state.isPaginating) {
                            item(key = "advanced-search-pagination") {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.TopCenter,
                                ) {
                                    Box(modifier = Modifier.widthIn(max = AdvancedSearchContentMaxWidth)) {
                                        PaginationLoadingIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedSearchFiltersCard(
    state: AdvancedSearchScreenState,
    actions: AdvancedSearchActions,
    onSearchImeAction: () -> Unit,
    onClearQueryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AdvancedSearchPanel(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(resource = Res.string.advanced_search_screen_filters_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.query,
            onValueChange = actions::onQueryChanged,
            singleLine = true,
            label = {
                Text(text = stringResource(resource = Res.string.advanced_search_screen_query_label))
            },
            placeholder = {
                Text(text = stringResource(resource = Res.string.advanced_search_screen_query_placeholder))
            },
            leadingIcon = {
                Icon(
                    imageVector = vectorResource(resource = Res.drawable.ic_search),
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = onClearQueryClicked) {
                        Icon(
                            imageVector = vectorResource(resource = Res.drawable.ic_close),
                            contentDescription = null,
                        )
                    }
                }
            },
            isError = state.validationError == AdvancedSearchValidationError.QueryRequired,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { onSearchImeAction() },
            ),
        )

        AdvancedSearchFilterSection(
            title = stringResource(resource = Res.string.advanced_search_screen_sort_label),
        ) {
            FilterChipsRow {
                SearchSort.entries.forEach { sort ->
                    FilterChip(
                        selected = state.sort == sort,
                        onClick = { actions.onSortSelected(sort) },
                        label = {
                            Text(text = searchSortLabel(sort))
                        },
                    )
                }
            }
        }

        AdvancedSearchFilterSection(
            title = stringResource(resource = Res.string.advanced_search_screen_votes_label),
        ) {
            FilterChipsRow {
                AdvancedSearchVoteOptions.forEach { minimumVotes ->
                    FilterChip(
                        selected = state.minimumVotes == minimumVotes,
                        onClick = { actions.onMinimumVotesSelected(minimumVotes) },
                        label = {
                            Text(
                                text = minimumVotes?.let { "$it+" }
                                    ?: stringResource(resource = Res.string.advanced_search_screen_votes_all),
                            )
                        },
                    )
                }
            }
        }

        AdvancedSearchFilterSection(
            title = stringResource(resource = Res.string.advanced_search_screen_date_label),
        ) {
            FilterChipsRow {
                AdvancedSearchDatePreset.entries.forEach { preset ->
                    FilterChip(
                        selected = state.datePreset == preset,
                        onClick = { actions.onDatePresetSelected(preset) },
                        label = {
                            Text(text = datePresetLabel(preset))
                        },
                    )
                }
            }
        }

        if (state.isCustomDateRangeVisible) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (maxWidth >= AdvancedSearchWideFieldBreakpoint) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CustomDateField(
                            modifier = Modifier.weight(1f),
                            value = state.customDateFrom,
                            label = stringResource(resource = Res.string.advanced_search_screen_date_from_label),
                            isError = state.validationError == AdvancedSearchValidationError.InvalidCustomDateFormat ||
                                state.validationError == AdvancedSearchValidationError.InvalidCustomDateRange,
                            onValueChange = actions::onCustomDateFromChanged,
                        )
                        CustomDateField(
                            modifier = Modifier.weight(1f),
                            value = state.customDateTo,
                            label = stringResource(resource = Res.string.advanced_search_screen_date_to_label),
                            isError = state.validationError == AdvancedSearchValidationError.InvalidCustomDateFormat ||
                                state.validationError == AdvancedSearchValidationError.InvalidCustomDateRange,
                            onValueChange = actions::onCustomDateToChanged,
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CustomDateField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.customDateFrom,
                            label = stringResource(resource = Res.string.advanced_search_screen_date_from_label),
                            isError = state.validationError == AdvancedSearchValidationError.InvalidCustomDateFormat ||
                                state.validationError == AdvancedSearchValidationError.InvalidCustomDateRange,
                            onValueChange = actions::onCustomDateFromChanged,
                        )
                        CustomDateField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.customDateTo,
                            label = stringResource(resource = Res.string.advanced_search_screen_date_to_label),
                            isError = state.validationError == AdvancedSearchValidationError.InvalidCustomDateFormat ||
                                state.validationError == AdvancedSearchValidationError.InvalidCustomDateRange,
                            onValueChange = actions::onCustomDateToChanged,
                        )
                    }
                }
            }
        }

        LabeledInputField(
            label = stringResource(resource = Res.string.advanced_search_screen_tags_label),
            value = state.tags,
            placeholder = null,
            onValueChange = actions::onTagsChanged,
        )

        LabeledInputField(
            label = stringResource(resource = Res.string.advanced_search_screen_users_label),
            value = state.users,
            placeholder = null,
            onValueChange = actions::onUsersChanged,
        )

        LabeledInputField(
            label = stringResource(resource = Res.string.advanced_search_screen_domains_label),
            value = state.domains,
            placeholder = null,
            onValueChange = actions::onDomainsChanged,
        )

        Text(
            text = stringResource(resource = Res.string.advanced_search_screen_multi_value_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.validationError?.let { validationError ->
            Text(
                text = validationErrorLabel(validationError),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = actions::onSearchClicked,
                enabled = state.isSearchButtonEnabled,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = vectorResource(resource = Res.drawable.ic_search),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = stringResource(resource = Res.string.advanced_search_screen_search_button))
            }
        }
    }
}

@Composable
private fun CustomDateField(
    value: String,
    label: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(text = label) },
        placeholder = {
            Text(text = stringResource(resource = Res.string.advanced_search_screen_date_placeholder))
        },
        isError = isError,
    )
}

@Composable
private fun LabeledInputField(
    label: String,
    placeholder: String?,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        placeholder = placeholder?.let { { Text(text = placeholder) } },
        minLines = 1,
        maxLines = 3,
    )
}

@Composable
private fun AdvancedSearchMessagePanel(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClicked: (() -> Unit)? = null,
) {
    AdvancedSearchPanel(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onActionClicked != null) {
            TextButton(onClick = onActionClicked) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
private fun AdvancedSearchPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun AdvancedSearchFilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        content()
    }
}

@Composable
private fun FilterChipsRow(
    content: @Composable () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun searchSortLabel(sort: SearchSort): String = when (sort) {
    SearchSort.Score -> stringResource(resource = Res.string.advanced_search_screen_sort_score)
    SearchSort.Popular -> stringResource(resource = Res.string.advanced_search_screen_sort_popular)
    SearchSort.Comments -> stringResource(resource = Res.string.advanced_search_screen_sort_comments)
    SearchSort.Newest -> stringResource(resource = Res.string.advanced_search_screen_sort_newest)
}

@Composable
private fun datePresetLabel(preset: AdvancedSearchDatePreset): String = when (preset) {
    AdvancedSearchDatePreset.AnyTime -> stringResource(resource = Res.string.advanced_search_screen_date_all)
    AdvancedSearchDatePreset.Last24Hours -> stringResource(resource = Res.string.advanced_search_screen_date_24h)
    AdvancedSearchDatePreset.Last3Days -> stringResource(resource = Res.string.advanced_search_screen_date_3d)
    AdvancedSearchDatePreset.Last7Days -> stringResource(resource = Res.string.advanced_search_screen_date_7d)
    AdvancedSearchDatePreset.Last30Days -> stringResource(resource = Res.string.advanced_search_screen_date_30d)
    AdvancedSearchDatePreset.LastYear -> stringResource(resource = Res.string.advanced_search_screen_date_year)
    AdvancedSearchDatePreset.Custom -> stringResource(resource = Res.string.advanced_search_screen_custom_date)
}

@Composable
private fun validationErrorLabel(error: AdvancedSearchValidationError): String = when (error) {
    AdvancedSearchValidationError.QueryRequired -> stringResource(resource = Res.string.advanced_search_screen_query_required)

    AdvancedSearchValidationError.InvalidCustomDateFormat -> stringResource(
        resource = Res.string.advanced_search_screen_invalid_date_format,
    )

    AdvancedSearchValidationError.InvalidCustomDateRange -> stringResource(
        resource = Res.string.advanced_search_screen_invalid_date_range,
    )
}
