package co.ltlabs.ltmechanic.domain

class MachineInStation (
    var id: Long,
    var station: String,
    var machine: String,
    var rfid: String,
    var subType: String,
    var index: Int = 0,
    var selected: Boolean = false,
    var macSubTypeId_desc: String? = "",
    var empty: Boolean = false,
    var building: String? = null,
    var buildingId: Int? = null,
)