package co.ltlabs.ltmechanic.domain.maint

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ltlabs.ltmechanic.constant.type.MaintType
import co.ltlabs.ltmechanic.domain.Ticket
import com.google.gson.annotations.SerializedName
import java.util.*

data class MaintResponse(

	@field:SerializedName("result")
	val maints: List<MaintItem>? = null,

	@field:SerializedName("meta")
	val meta: Meta? = null
)

data class MaintItem(

	@field:SerializedName("closedBy")
	val closedBy: String? = null,

	@field:SerializedName("checkListId")
	val checkListId: Int? = null,

	@field:SerializedName("solutionTypeId")
	val solutionTypeId: Int? = null,

	@field:SerializedName("canceledBy")
	val canceledBy: String? = null,

	@field:SerializedName("lineName")
	val lineName: String? = null,

	@field:SerializedName("ticketTypeId")
	val ticketTypeId: Int? = null,

	@field:SerializedName("repairedDt")
	val repairedDt: Date? = null,

	@field:SerializedName("canceledDt")
	val canceledDt: Date? = null,

	@field:SerializedName("lineCode")
	val lineCode: String? = null,

	@field:SerializedName("problemTypeId")
	val problemTypeId: Any? = null,

	@field:SerializedName("ticketNo")
	val ticketNo: String? = null,

	@field:SerializedName("mfgLineId")
	val mfgLineId: Int? = null,

	@field:SerializedName("areaName")
	val areaName: String? = null,

	@field:SerializedName("station")
	val station: String? = null,

	@field:SerializedName("statusName")
	val status: MaintType? = null,

	@field:SerializedName("machineNo")
	val machineNo: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("moduleId")
	val moduleId: Int? = null,

	@field:SerializedName("nextMaintDate")
	val nextMaintDate: Date? = null,

	@field:SerializedName("reportedDt")
	val reportedDt: Date? = null,

	@field:SerializedName("reopenedDt")
	val reopenedDt: Date? = null,

	@field:SerializedName("reportedBy")
	val reportedBy: String? = null,

	@field:SerializedName("areaCode")
	val areaCode: String? = null,

	@field:SerializedName("machineId")
	val machineId: Int? = null,

	@field:SerializedName("areaId")
	val areaId: Int? = null,

	@field:SerializedName("statusId")
	val statusId: Int? = null,

	@field:SerializedName("reopenedBy")
	val reopenedBy: String? = null,

	@field:SerializedName("repairedBy")
	val repairedBy: String? = null,

	@field:SerializedName("grabbedBy")
	val grabbedBy: String? = null,

	@field:SerializedName("remarks")
	val remarks: String? = null,

	@field:SerializedName("grabbedDt")
	val grabbedDt: Date? = null,

	@field:SerializedName("closedDt")
	val closedDt: Date? = null,

	@field:SerializedName("statusCode")
	val statusCode: String? = null,

	@field:SerializedName("dateCount")
	val dateCount: Int? = null,

	@field:SerializedName("place")
	val place: String? = null,
)

@Entity(tableName = "meta_maint")
data class Meta(
	@PrimaryKey
	var type: String,
	val pageSize: String? = null,
	val page: Int? = null,
	val totalRecord: Int? = null
)
