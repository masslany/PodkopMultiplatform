package pl.masslany.podkop.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.theme.colorsPalette
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.links_screen_label_adult_rating

@Composable
fun AdultRating(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .background(
                color = MaterialTheme.colorsPalette.adultRed,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(
                horizontal = 4.dp,
                vertical = 1.dp
            )
    ) {
        Text(
            text = stringResource(resource = Res.string.links_screen_label_adult_rating),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
        )
    }
}
