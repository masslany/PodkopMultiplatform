package pl.masslany.podkop.features.linksubmission.addlink.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.masslany.podkop.composeapp.R

@Composable
internal fun CurrentUrlField(url: String) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = url,
        onValueChange = {},
        label = {
            Text(text = stringResource(id = R.string.add_link_url_label))
        },
        readOnly = true,
        minLines = 2,
    )
}
