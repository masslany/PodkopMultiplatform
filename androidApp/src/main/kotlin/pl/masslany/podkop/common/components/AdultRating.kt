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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.masslany.podkop.R
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette

@Composable
fun AdultRating(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .background(
                color = MaterialTheme.colorsPalette.adultRed,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(
                horizontal = 4.dp,
                vertical = 1.dp,
            ),
    ) {
        Text(
            text = stringResource(id = R.string.links_screen_label_adult_rating),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        )
    }
}

@Preview
@Composable
private fun AdultRatingPreview() {
    PodkopPreview(darkTheme = false) {
        AdultRating()
    }
}
