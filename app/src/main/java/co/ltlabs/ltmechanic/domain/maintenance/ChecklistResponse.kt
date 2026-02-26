package co.ltlabs.ltmechanic.domain.maintenance

import android.util.Log
import com.google.gson.annotations.SerializedName

data class ChecklistResponse(

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("machines")
	val machines: Machines? = null
)

data class ChecklistItem(

	@field:SerializedName("no")
	val no: String? = null,

	@field:SerializedName("macSubTypeId")
	val macSubTypeId: Any? = null,

	@field:SerializedName("tasksSteps")
	val tasksSteps: Int? = null,

	@field:SerializedName("checklistType")
	val checklistType: String? = null,

	@field:SerializedName("isActive")
	val isActive: Boolean? = null,

	@field:SerializedName("frequency")
	val frequency: String? = null,

	@field:SerializedName("templateStatus")
	val templateStatus: String? = null,

	@field:SerializedName("createdDt")
	val createdDt: String? = null,

	@field:SerializedName("createdBy")
	val createdBy: String? = null,

	@field:SerializedName("brandId")
	val brandId: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("macsubtype_desc")
	val macsubtypeDesc: String? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("tempTypeId")
	val tempTypeId: Int? = null,

	@field:SerializedName("brand")
	val brand: String? = null,

	@field:SerializedName("macsubtype")
	val macsubtype: String? = null,

	@field:SerializedName("subTasksSteps")
	val subTasksSteps: Int? = null,

	var checked: Boolean = false
)

data class Meta(

	@field:SerializedName("pageCount")
	val pageCount: Int? = null,

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("pageSize")
	val pageSize: String? = null,

	@field:SerializedName("currentPage")
	val currentPage: String? = null
)

data class Machines(

	@field:SerializedName("result")
	val result: List<ChecklistItem>? = null,

	@field:SerializedName("meta")
	val meta: Meta? = null
)
