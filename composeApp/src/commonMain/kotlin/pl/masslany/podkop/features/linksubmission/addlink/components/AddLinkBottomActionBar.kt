package pl.masslany.podkop.features.linksubmission.addlink.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.add_link_action_cancel

@Composable
internal fun AddLinkBottomActionBar(
    primaryLabel: StringResource,
    isPrimaryEnabled: Boolean,
    isLoading: Boolean,
    onPrimaryClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isCancelEnabled: Boolean = true,
) {
    Surface(
        modifier = modifier.imePadding(),
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onCancelClicked,
                enabled = isCancelEnabled,
            ) {
                Text(text = stringResource(resource = Res.string.add_link_action_cancel))
            }
            Button(
                onClick = onPrimaryClicked,
                enabled = isPrimaryEnabled,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = stringResource(resource = primaryLabel))
                }
            }
        }
    }
}
