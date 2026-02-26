package co.ltlabs.ltmechanic.domain.changeover

import android.os.Parcelable
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.constant.type.IsCriticalMachineType
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

data class COResponse(

    @field:SerializedName("corequest")
    val coRequest: CORequest? = null
)

data class CORequest(

    @field:SerializedName("area")
    val area: String? = null,

    @field:SerializedName("updatedBy")
    val updatedBy: String? = null,

    @field:SerializedName("coRequestDt")
    val coRequestDt: Date? = null,

    @field:SerializedName("factoryId")
    val factoryId: Int? = null,

    @field:SerializedName("isActive")
    val isActive: Int? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("mcQty")
    val mcQty: Int? = null,

    @field:SerializedName("mfgline")
    val mfgLine: String? = null,

    @field:SerializedName("changeoverTypeId")
    val changeoverTypeId: Int? = null,

    @field:SerializedName("createdDt")
    val createdDt: Date? = null,

    @field:SerializedName("coRequestNo")
    val coRequestNo: String? = null,

    @field:SerializedName("areaId")
    val areaId: Int? = null,

    @field:SerializedName("statusId")
    val statusId: Int? = null,

    @field:SerializedName("mfgLineId")
    val mfgLineId: Int? = null,

    @field:SerializedName("createdBy")
    val createdBy: String? = null,

    @field:SerializedName("criticalMcQty")
    val criticalMcQty: Int? = null,

    @field:SerializedName("style")
    val style: String? = null,

    @field:SerializedName("updatedDt")
    val updatedDt: Date? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("productType")
    val productType: String? = null,

    @field:SerializedName("remarks")
    val remarks: String? = null,

    @field:SerializedName("statusDesc")
    val status: COStatusType? = null,

    @field:SerializedName("items")
    val items: List<OperatorItem?>? = null,

    @field:SerializedName("checkList")
    val checkList: List<CheckListItem?>? = null,
)

data class OperatorItem(

    @field:SerializedName("operations")
    val operations: List<OperationItem?>? = null,

    @field:SerializedName("station")
    val station: Int? = null
)

@Parcelize
data class OperationItem(

    @field:SerializedName("macType")
    val macType: String? = null,

    @field:SerializedName("macTypeName")
    val macTypeName: String? = null,

    @field:SerializedName("updatedBy")
    val updatedBy: String? = null,

    @field:SerializedName("attachments")
    val attachments: List<AttachmentsItem?>? = null,

    @field:SerializedName("logs")
    val logs: List<LogsItem?>? = null,

    @field:SerializedName("macSubTypeId")
    val macSubTypeId: Int? = null,

    @field:SerializedName("coRequestId")
    val coRequestId: Int? = null,

    @field:SerializedName("subType")
    val macSubType: String? = null,

    @field:SerializedName("subTypeName")
    val macSubTypeName: String? = null,

    @field:SerializedName("createdDt")
    val createdDt: Date? = null,

    @field:SerializedName("createdBy")
    val createdBy: String? = null,

    @field:SerializedName("macTypeId")
    val macTypeId: Int? = null,

    @field:SerializedName("isCritical")
    val isCritical: IsCriticalMachineType? = null,

    @field:SerializedName("updatedDt")
    val updatedDt: Date? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("operation")
    val operation: String? = null,

    @field:SerializedName("remarks")
    val remarks: String? = null,

    @field:SerializedName("needleType")
    val needleType: String? = null,

    @field:SerializedName("machineId")
    val machineId: Int? = null,

    @field:SerializedName("machineNo")
    val machine: String? = null,

    @field:SerializedName("note")
    val note: String? = null,

    @field:SerializedName("mfgline")
    val line: String? = null,

    @field:SerializedName("area")
    val area: String? = null,

    @field:SerializedName("statusDesc")
    val status: COStatusType? = COStatusType.New,

    @field:SerializedName("machineStatus")
    val machineStatus: String? = null,

    var isExist: Boolean = false

) : Parcelable

@Parcelize
data class AttachmentsItem(

    @field:SerializedName("desc1")
    val desc1: String? = null,

    @field:SerializedName("imgLink")
    val imgLink: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    var isChecked: Boolean = false

) : Parcelable

@Parcelize
data class CheckListItem(

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("checkListId")
    val checkListId: Int? = null,

    @field:SerializedName("task")
    val task: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    var isChecked: Boolean = false

) : Parcelable

@Parcelize
data class LogsItem(

    @field:SerializedName("updatedBy")
    val updatedBy: String? = null,

    @field:SerializedName("updatedDt")
    val updatedDt: Date? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("coRequestItemsId")
    val coRequestItemsId: String? = null,

    @field:SerializedName("status")
    val status: String? = null

) : Parcelable
