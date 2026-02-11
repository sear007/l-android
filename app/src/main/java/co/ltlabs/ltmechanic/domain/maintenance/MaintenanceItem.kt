package co.ltlabs.ltmechanic.domain.maintenance

import com.google.gson.annotations.SerializedName
import java.util.*

data class MaintenanceItem(

    @field:SerializedName("mfgline")
    val mfgLine: String? = null,

    @field:SerializedName("createdDt")
    val createdDate: Date? = null,

    @field:SerializedName("coRequestDt")
    val coRequestDate: Date? = null,

    @field:SerializedName("coRequestNo")
    val coRequestNo: String? = null,

    @field:SerializedName("updatedBy")
    val updatedBy: String? = null,

    @field:SerializedName("criticalMcQty")
    val criticalMcQty: Int? = null,

    @field:SerializedName("createdBy")
    val createdBy: String? = null,

    @field:SerializedName("style")
    val style: String? = null,

    @field:SerializedName("updatedDt")
    val updatedDate: Date? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("mcQty")
    val mcQty: Int? = null,

    @field:SerializedName("statusDesc")
    val status: String? = null,

    @field:SerializedName("dateCount")
    val dateCount: Int? = null
)
