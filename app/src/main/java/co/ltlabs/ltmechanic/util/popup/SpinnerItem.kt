package co.ltlabs.ltmechanic.util.popup

data class SpinnerItem(
    val name: String,
    val desc: String = "",
    var checked: Boolean = false,
    var position: Int = 0,
    var id: Long = -1L
)
