package pl.masslany.podkop.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_comment

@Composable
fun CommentCount(
    modifier: Modifier = Modifier,
    count: Int,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.ic_comment),
            contentDescription = null,
            modifier = Modifier
                .size(16.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
