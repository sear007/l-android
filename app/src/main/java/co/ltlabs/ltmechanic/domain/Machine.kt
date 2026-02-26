package co.ltlabs.ltmechanic.domain

import co.ltlabs.ltmechanic.util.toBoolean
import java.util.*

data class Machine (
    val machine: String,
    var station: String,
    val rfid: String?,
    val areaId:Long?,
    val id: Long,
    var subtype: String? = "",
    val macSubTypeId: Long?,
    val mfgLineId: Long?,
    val mfgLine: String?,
    val brandId: Long,
    val brand: String,
    val status: String,
    var hasOpenTickets: Any = 1,
    var hasOpenTicket: Boolean = hasOpenTickets.toBoolean(),
    val area: String,
    val lastRepairedDt: Date?,
    val supplierDt: Date?,
    val condition: String,
    val lastPMDt: Date?,
    val maintenanceFreq: String,
    val rental: String,
    val building: String?,
    val buildingId: Int
)