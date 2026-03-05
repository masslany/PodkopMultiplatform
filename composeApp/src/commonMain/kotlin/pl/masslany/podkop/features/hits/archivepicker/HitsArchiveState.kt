package pl.masslany.podkop.features.hits.archivepicker

data class HitsArchiveState(val year: Int, val month: Int) {
    val label: String = "${month.toString().padStart(2, '0')}.$year"
}
