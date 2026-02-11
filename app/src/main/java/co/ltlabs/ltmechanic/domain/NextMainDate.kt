package co.ltlabs.ltmechanic.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class NextMainDate(
	val reportedDate: Date? = null,
	val nextMainDate: Date? = null
) : Parcelable
