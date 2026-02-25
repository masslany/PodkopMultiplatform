package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_resource_more_actions
import podkop.composeapp.generated.resources.ic_more_vert
import podkop.composeapp.generated.resources.ic_reply
import podkop.composeapp.generated.resources.ic_star
import podkop.composeapp.generated.resources.resource_actions_reply

@Composable
internal fun ResourceInlineActionsRow(
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val disabledTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = vectorResource(resource = Res.drawable.ic_reply),
                    contentDescription = null,
                    tint = disabledTint,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(resource = Res.string.resource_actions_reply),
                    style = MaterialTheme.typography.labelLarge,
                    color = disabledTint,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                enabled = false,
                modifier = Modifier.size(32.dp),
                onClick = {},
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = vectorResource(resource = Res.drawable.ic_star),
                    contentDescription = null,
                    tint = disabledTint,
                )
            }
        }

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = onMoreClick,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = vectorResource(resource = Res.drawable.ic_more_vert),
                contentDescription = stringResource(
                    resource = Res.string.accessibility_resource_more_actions,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun ResourceInlineActionsRowPreview() {
    PodkopPreview(darkTheme = false) {
        ResourceInlineActionsRow(
            onMoreClick = {},
        )
    }
}
