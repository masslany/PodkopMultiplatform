package pl.masslany.podkop.features.links.hits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.links_screen_label_hits

@Composable
fun HitsList(
    modifier: Modifier,
    state: ImmutableList<ResourceItemState>,
    actions: ResourceItemActions,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(resource = Res.string.links_screen_label_hits),
        )
        Spacer(modifier = Modifier.size(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(
                items = state,
                key = { item -> item.id },
            ) {
                ResourceItemRenderer(
                    state = it,
                    actions = actions,
                )
            }
        }
    }
}
