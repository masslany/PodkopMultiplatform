package pl.masslany.podkop.features.links

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LinksViewModel(
    val isUpcoming: Boolean,
) : ViewModel(), LinksActions {
    
    val state: StateFlow<LinksScreenState>
        field = MutableStateFlow<LinksScreenState>(LinksScreenState.initial)

    init {
        println("MEOW isUpcoming: $isUpcoming")
        state.update {
            it.copy(
                isUpcoming = isUpcoming,
            )
        }
    }
    
    override fun onLinkClicked(id: String) {
        TODO("Not yet implemented")
    }

    override fun onLinkVoteClicked(id: String) {
        TODO("Not yet implemented")
    }

    override fun onHitClicked(id: String) {
        TODO("Not yet implemented")
    }

    override fun onHitVoteClicked(id: String) {
        TODO("Not yet implemented")
    }

}
