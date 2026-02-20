package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.profile_log_in_button
import podkop.composeapp.generated.resources.profile_not_logged_in_message
import podkop.composeapp.generated.resources.user_profile_not_logged_in

@Composable
fun ProfileLoggedOutContent(
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            modifier = Modifier.size(240.dp),
            painter = painterResource(resource = Res.drawable.user_profile_not_logged_in),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = stringResource(resource = Res.string.profile_not_logged_in_message))
        Button(onClick = onLoginClicked) {
            Text(text = stringResource(resource = Res.string.profile_log_in_button))
        }
    }
}
