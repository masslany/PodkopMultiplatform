package pl.masslany.podkop.features.resources.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_resource_reply_close
import podkop.composeapp.generated.resources.entry_details_reply_composer_hint
import podkop.composeapp.generated.resources.entry_details_reply_composer_send
import podkop.composeapp.generated.resources.entry_details_reply_composer_target
import podkop.composeapp.generated.resources.ic_close

@Composable
fun ReplyComposer(
    isVisible: Boolean,
    content: String,
    replyTarget: String?,
    isSubmitting: Boolean,
    onContentChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val systemBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        val bottomPadding = if (imeBottomPadding > systemBottomPadding) {
            imeBottomPadding
        } else {
            systemBottomPadding
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = bottomPadding + 8.dp,
                ),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                replyTarget?.let { target ->
                    Text(
                        text = stringResource(
                            resource = Res.string.entry_details_reply_composer_target,
                            target,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = content,
                    onValueChange = onContentChanged,
                    enabled = !isSubmitting,
                    minLines = 3,
                    maxLines = 6,
                    placeholder = {
                        Text(text = stringResource(resource = Res.string.entry_details_reply_composer_hint))
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            onSubmit()
                        },
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        enabled = !isSubmitting,
                        onClick = onDismiss,
                    ) {
                        Icon(
                            imageVector = vectorResource(resource = Res.drawable.ic_close),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_resource_reply_close,
                            ),
                        )
                    }

                    Button(
                        onClick = onSubmit,
                        enabled = !isSubmitting && content.isNotBlank(),
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = stringResource(resource = Res.string.entry_details_reply_composer_send),
                            )
                        }
                    }
                }
            }
        }
    }
}
