package pl.masslany.podkop.features.linksubmission.linkdraft.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.features.linksubmission.models.AddLinkTagSuggestionState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_reply_composer_remove_photo
import podkop.composeapp.generated.resources.add_link_tag_helper
import podkop.composeapp.generated.resources.add_link_tag_hint
import podkop.composeapp.generated.resources.add_link_tag_label
import podkop.composeapp.generated.resources.ic_close
import podkop.composeapp.generated.resources.search_screen_tags_followers

@Composable
internal fun LinkDraftTagInputField(
    tagInput: String,
    tagSuggestions: ImmutableList<AddLinkTagSuggestionState>,
    isLoadingTagSuggestions: Boolean,
    onTagInputChanged: (String) -> Unit,
    onPendingTagSubmitted: () -> Unit,
    onSuggestionClicked: (String) -> Unit,
) {
    var anchorWidthPx by remember { mutableStateOf(0) }

    Box {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidthPx = it.width },
            value = tagInput,
            onValueChange = onTagInputChanged,
            label = {
                Text(text = stringResource(resource = Res.string.add_link_tag_label))
            },
            placeholder = {
                Text(text = stringResource(resource = Res.string.add_link_tag_hint))
            },
            supportingText = {
                Text(text = stringResource(resource = Res.string.add_link_tag_helper))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onPendingTagSubmitted() },
            ),
        )

        if (anchorWidthPx > 0 && (isLoadingTagSuggestions || tagSuggestions.isNotEmpty())) {
            TagSuggestionsPopup(
                anchorWidthPx = anchorWidthPx,
                suggestions = tagSuggestions,
                isLoading = isLoadingTagSuggestions,
                onSuggestionClicked = onSuggestionClicked,
            )
        }
    }
}

@Composable
internal fun AddLinkTagChip(
    tag: String,
    onRemoveClicked: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(800.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                modifier = Modifier.size(18.dp),
                onClick = onRemoveClicked,
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = vectorResource(resource = Res.drawable.ic_close),
                    contentDescription = stringResource(resource = Res.string.accessibility_reply_composer_remove_photo),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun TagSuggestionsPopup(
    anchorWidthPx: Int,
    suggestions: ImmutableList<AddLinkTagSuggestionState>,
    isLoading: Boolean,
    onSuggestionClicked: (String) -> Unit,
) {
    val density = LocalDensity.current
    val popupPositionProvider = remember(density) {
        AboveAnchorPopupPositionProvider(verticalMarginPx = with(density) { 8.dp.roundToPx() })
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
            if (isLoading) {
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
            } else {
                Column {
                    suggestions.forEachIndexed { index, suggestion ->
                        TagSuggestionRow(
                            suggestion = suggestion,
                            onClick = { onSuggestionClicked(suggestion.name) },
                        )
                        if (index != suggestions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

private class AboveAnchorPopupPositionProvider(private val verticalMarginPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = anchorBounds.left.coerceIn(0, maxX)
        val preferredAbove = anchorBounds.top - popupContentSize.height - verticalMarginPx
        val fallbackBelow = anchorBounds.bottom + verticalMarginPx
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        val y = if (preferredAbove >= 0) preferredAbove else fallbackBelow.coerceAtMost(maxY)
        return IntOffset(x = x, y = y)
    }
}

@Composable
private fun TagSuggestionRow(
    suggestion: AddLinkTagSuggestionState,
    onClick: () -> Unit,
) {
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
                suggestion.observedQuantity,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
