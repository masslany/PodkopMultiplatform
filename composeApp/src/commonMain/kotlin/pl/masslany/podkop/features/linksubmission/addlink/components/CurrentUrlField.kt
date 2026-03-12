package pl.masslany.podkop.features.linksubmission.addlink.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.add_link_url_label

@Composable
internal fun CurrentUrlField(url: String) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = url,
        onValueChange = {},
        label = {
            Text(text = stringResource(resource = Res.string.add_link_url_label))
        },
        readOnly = true,
        minLines = 2,
    )
}
