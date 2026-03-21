package pl.masslany.podkop.features.rank.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_arrow_down
import podkop.composeapp.generated.resources.ic_arrow_up

@Composable
fun RankTrendIndicator(
    trend: Int,
    modifier: Modifier = Modifier,
) {
    when {
        trend > 0 -> {
            Icon(
                modifier = modifier.size(18.dp),
                imageVector = vectorResource(resource = Res.drawable.ic_arrow_up),
                contentDescription = null,
                tint = MaterialTheme.colorsPalette.votePositive,
            )
        }

        trend < 0 -> {
            Icon(
                modifier = modifier.size(18.dp),
                imageVector = vectorResource(resource = Res.drawable.ic_arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colorsPalette.voteNegative,
            )
        }

        else -> {
            Spacer(modifier = modifier.width(18.dp))
        }
    }
}

@Preview
@Composable
private fun RankTrendIndicatorPreview() {
    PodkopPreview(darkTheme = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RankTrendIndicator(trend = -1)
            RankTrendIndicator(trend = 0)
            RankTrendIndicator(trend = 1)
        }
    }
}
