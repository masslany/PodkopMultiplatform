package pl.masslany.podkop.features.blacklists.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.extensions.rememberWindowSizeClass
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryState
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType
import pl.masslany.podkop.features.blacklists.models.BlacklistSuggestionItemState
import pl.masslany.podkop.features.blacklists.models.BlacklistSuggestionsState
import pl.masslany.podkop.features.blacklists.models.BlacklistSuggestionsStatus
import pl.masslany.podkop.features.blacklists.models.BlacklistedTagSuggestionItemState
import pl.masslany.podkop.features.blacklists.models.BlacklistedUserSuggestionItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.blacklists_button_add
import podkop.composeapp.generated.resources.blacklists_input_domain_hint
import podkop.composeapp.generated.resources.blacklists_input_domain_label
import podkop.composeapp.generated.resources.blacklists_input_tag_hint
import podkop.composeapp.generated.resources.blacklists_input_tag_label
import podkop.composeapp.generated.resources.blacklists_input_user_hint
import podkop.composeapp.generated.resources.blacklists_input_user_label
import podkop.composeapp.generated.resources.blacklists_suggestions_error
import podkop.composeapp.generated.resources.refresh_button
import podkop.composeapp.generated.resources.search_screen_no_results
import podkop.composeapp.generated.resources.search_screen_tags_followers

@Composable
internal fun BlacklistAddForm(
    state: BlacklistCategoryState,
    onInputChanged: (String) -> Unit,
    onAddClicked: () -> Unit,
    onSuggestionClicked: (String) -> Unit,
    onRetrySuggestionsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = rememberWindowSizeClass()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BlacklistAddInputField(
                        state = state,
                        onInputChanged = onInputChanged,
                        onAddClicked = onAddClicked,
                        onSuggestionClicked = onSuggestionClicked,
                        onRetrySuggestionsClicked = onRetrySuggestionsClicked,
                    )
                    AddButton(
                        isLoading = state.isActionsInProgress,
                        enabled = state.canSubmit,
                        onClick = onAddClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                val buttonWidth = 180.dp
                val inputWidth = (maxWidth - buttonWidth - 12.dp).coerceAtLeast(0.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BlacklistAddInputField(
                        state = state,
                        onInputChanged = onInputChanged,
                        onAddClicked = onAddClicked,
                        onSuggestionClicked = onSuggestionClicked,
                        onRetrySuggestionsClicked = onRetrySuggestionsClicked,
                        modifier = Modifier.width(inputWidth),
                    )
                    AddButton(
                        isLoading = state.isActionsInProgress,
                        enabled = state.canSubmit,
                        onClick = onAddClicked,
                        modifier = Modifier.width(buttonWidth),
                    )
                }
            }
        }
    }
}

@Composable
private fun BlacklistAddInputField(
    state: BlacklistCategoryState,
    onInputChanged: (String) -> Unit,
    onAddClicked: () -> Unit,
    onSuggestionClicked: (String) -> Unit,
    onRetrySuggestionsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var anchorWidthPx by remember { mutableStateOf(0) }

    Box(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidthPx = it.width },
            value = state.addInput,
            onValueChange = onInputChanged,
            enabled = !state.isActionsInProgress,
            label = {
                Text(text = state.type.inputLabel())
            },
            placeholder = {
                Text(text = state.type.inputHint())
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onAddClicked() },
            ),
        )

        if (state.type != BlacklistCategoryType.Domains &&
            anchorWidthPx > 0 &&
            state.suggestions.status != BlacklistSuggestionsStatus.Hidden
        ) {
            SuggestionsPopup(
                anchorWidthPx = anchorWidthPx,
                state = state,
                onSuggestionClicked = onSuggestionClicked,
                onRetrySuggestionsClicked = onRetrySuggestionsClicked,
            )
        }
    }
}

@Composable
private fun AddButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = stringResource(resource = Res.string.blacklists_button_add))
        }
    }
}

@Composable
private fun SuggestionsPopup(
    anchorWidthPx: Int,
    state: BlacklistCategoryState,
    onSuggestionClicked: (String) -> Unit,
    onRetrySuggestionsClicked: () -> Unit,
) {
    val density = LocalDensity.current
    val popupPositionProvider = remember(density) {
        BelowAnchorPopupPositionProvider(verticalMarginPx = with(density) { 8.dp.roundToPx() })
    }
    val popupWidth = with(density) { anchorWidthPx.toDp() }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = {},
        properties = PopupProperties(focusable = false),
    ) {
        Surface(
            modifier = Modifier
                .width(popupWidth)
                .heightIn(max = 280.dp),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            when (state.suggestions.status) {
                BlacklistSuggestionsStatus.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }

                BlacklistSuggestionsStatus.Error -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(resource = Res.string.blacklists_suggestions_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onRetrySuggestionsClicked) {
                            Text(text = stringResource(resource = Res.string.refresh_button))
                        }
                    }
                }

                BlacklistSuggestionsStatus.Content -> {
                    if (state.suggestions.items.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = stringResource(resource = Res.string.search_screen_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column {
                            state.suggestions.items.forEachIndexed { index, suggestion ->
                                SuggestionRow(
                                    suggestion = suggestion,
                                    onClick = {
                                        onSuggestionClicked(
                                            when (suggestion) {
                                                is BlacklistedUserSuggestionItemState -> suggestion.username
                                                is BlacklistedTagSuggestionItemState -> suggestion.name
                                            },
                                        )
                                    },
                                )
                                if (index != state.suggestions.items.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }

                BlacklistSuggestionsStatus.Hidden -> Unit
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: BlacklistSuggestionItemState,
    onClick: () -> Unit,
) {
    when (suggestion) {
        is BlacklistedUserSuggestionItemState -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Avatar(
                    state = suggestion.avatarState,
                    onClick = onClick,
                )
                Text(
                    text = suggestion.username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = suggestion.nameColorType.toComposeColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        is BlacklistedTagSuggestionItemState -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "#${suggestion.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(
                        resource = Res.string.search_screen_tags_followers,
                        suggestion.followers,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BlacklistCategoryType.inputLabel(): String = when (this) {
    BlacklistCategoryType.Users -> stringResource(resource = Res.string.blacklists_input_user_label)
    BlacklistCategoryType.Tags -> stringResource(resource = Res.string.blacklists_input_tag_label)
    BlacklistCategoryType.Domains -> stringResource(resource = Res.string.blacklists_input_domain_label)
}

@Composable
private fun BlacklistCategoryType.inputHint(): String = when (this) {
    BlacklistCategoryType.Users -> stringResource(resource = Res.string.blacklists_input_user_hint)
    BlacklistCategoryType.Tags -> stringResource(resource = Res.string.blacklists_input_tag_hint)
    BlacklistCategoryType.Domains -> stringResource(resource = Res.string.blacklists_input_domain_hint)
}

private class BelowAnchorPopupPositionProvider(private val verticalMarginPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = anchorBounds.left.coerceIn(0, maxX)
        val preferredBelow = anchorBounds.bottom + verticalMarginPx
        val preferredAbove = anchorBounds.top - popupContentSize.height - verticalMarginPx
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        val y = if (preferredBelow + popupContentSize.height <= windowSize.height) {
            preferredBelow
        } else {
            preferredAbove.coerceIn(0, maxY)
        }
        return IntOffset(x = x, y = y)
    }
}

@Preview(name = "Compact", widthDp = 390)
@Composable
private fun BlacklistAddFormCompactPreview() {
    PodkopPreview(darkTheme = false) {
        BlacklistAddForm(
            modifier = Modifier.padding(16.dp),
            state = previewTagCategoryState(),
            onInputChanged = {},
            onAddClicked = {},
            onSuggestionClicked = {},
            onRetrySuggestionsClicked = {},
        )
    }
}

@Preview(name = "Wide", widthDp = 960)
@Composable
private fun BlacklistAddFormWidePreview() {
    PodkopPreview(darkTheme = false) {
        BlacklistAddForm(
            modifier = Modifier.padding(16.dp),
            state = previewDomainCategoryState(),
            onInputChanged = {},
            onAddClicked = {},
            onSuggestionClicked = {},
            onRetrySuggestionsClicked = {},
        )
    }
}

@Preview(name = "User Suggestion")
@Composable
private fun UserSuggestionRowPreview() {
    PodkopPreview(darkTheme = false) {
        Surface {
            SuggestionRow(
                suggestion = previewUserSuggestion(),
                onClick = {},
            )
        }
    }
}

@Preview(name = "Tag Suggestion")
@Composable
private fun TagSuggestionRowPreview() {
    PodkopPreview(darkTheme = false) {
        Surface {
            SuggestionRow(
                suggestion = previewTagSuggestion(),
                onClick = {},
            )
        }
    }
}

private fun previewTagCategoryState(): BlacklistCategoryState =
    BlacklistCategoryState.initial(BlacklistCategoryType.Tags).copy(
        isLoading = false,
        addInput = "ran",
        canSubmit = true,
        suggestions = BlacklistSuggestionsState(
            status = BlacklistSuggestionsStatus.Content,
            items = persistentListOf(
                previewTagSuggestion(),
                BlacklistedTagSuggestionItemState(
                    name = "randomanimeshit",
                    followers = 142,
                ),
            ),
        ),
    )

private fun previewDomainCategoryState(): BlacklistCategoryState =
    BlacklistCategoryState.initial(BlacklistCategoryType.Domains).copy(
        isLoading = false,
        addInput = "devkop.pl",
        canSubmit = true,
    )

private fun previewUserSuggestion(): BlacklistedUserSuggestionItemState =
    BlacklistedUserSuggestionItemState(
        username = "kobiaszu",
        avatarState = AvatarState(
            type = AvatarType.NetworkImage("https://picsum.photos/seed/blacklist-suggestion-user/96/96"),
            genderIndicatorType = GenderIndicatorType.Male,
        ),
        nameColorType = NameColorType.Orange,
    )

private fun previewTagSuggestion(): BlacklistedTagSuggestionItemState =
    BlacklistedTagSuggestionItemState(
        name = "randomanime",
        followers = 64,
    )
