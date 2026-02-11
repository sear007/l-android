package co.ltlabs.ltmechanic.util

import android.util.Log
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "DateUtil";

class DateUtil {

    companion object {

        const val SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
        const val SERVER_DATE_TIME_FORMAT_UTC_0 = "yyyy-MM-dd"
        private const val DATE_FORMAT = "dd/MM/yyyy"
        private const val TIME_FORMAT = "HH:mm"
        const val DATE_TIME_FORMAT = "$DATE_FORMAT $TIME_FORMAT"

        fun getDateTimeWithTimeZone(dateTime: String): DateTime {

//            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
//            val isoParsed = ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(dateTime)

            return ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(dateTime)

        }

        fun formatToDateAndTime(
            date: Date?,
            patter: String = DATE_TIME_FORMAT,
            default: String = "-"
        ): String {
            date ?: return default
            val format = SimpleDateFormat(patter, Locale.getDefault())
            return try {
                format.format(date)
            } catch (e: Exception) {
                default
            }
        }

        fun formatToDate(
            date: Date?,
            patter: String = DATE_FORMAT,
            default: String = "-"
        ): String {
            date ?: return default
            val format = SimpleDateFormat(patter, Locale.getDefault())
            return try {
                format.format(date)
            } catch (e: Exception) {
                default
            }
        }

        fun formatToTime(
            date: Date?,
            patter: String = TIME_FORMAT,
            default: String = "-"
        ): String {
            date ?: return default
            val format = SimpleDateFormat(patter, Locale.getDefault())
            return try {
                format.format(date)
            } catch (e: Exception) {
                default
            }
        }

        fun getDate(dateTime: DateTime): String {

            val action = dateTime.zone.toString()[0].toString()
            val zoneHours = dateTime.zone.toString().split(":")[0]
            val withZoneDateTime = if (action == "+") {
                dateTime.plusHours(zoneHours.toInt())
            } else {
                dateTime.minusHours(zoneHours.toInt())
            }

            val day = withZoneDateTime.dayOfMonth
            val month = withZoneDateTime.monthOfYear
            val year = withZoneDateTime.year

            val dayStr = if (day > 9) {
                "$day"
            } else {
                "0$day"
            }

            val monthStr = if (month > 9) {
                "$month"
            } else {
                "0$month"
            }

            return "$dayStr/$monthStr/$year"
        }

        fun getTime(dateTime: DateTime): String {

            val action = dateTime.zone.toString()[0].toString()
            val zoneHours = dateTime.zone.toString().split(":")[0]
            val zoneMinutes = dateTime.zone.toString().split(":")[1]

            Log.d(TAG, "getTime: zoneHours: $zoneHours")
            Log.d(TAG, "getTime: action: $action")
            val withZoneHours = if (action == "+") {
                dateTime.plusHours(zoneHours.toInt()).hourOfDay
            } else {
                dateTime.minusHours(zoneHours.replace("-", "").toInt()).hourOfDay
            }

            val withZoneMinutes = if (action == "+") {
                dateTime.plusMinutes(zoneMinutes.toInt()).minuteOfHour
            } else {
                dateTime.minusMinutes(zoneMinutes.toInt()).minuteOfHour
            }

            val withZoneHoursResult = if (withZoneHours > 9) {
                withZoneHours
            } else {
                "0$withZoneHours"
            }

            val withZoneMinutesResult = if (withZoneMinutes > 9) {
                withZoneMinutes
            } else {
                "0$withZoneMinutes"
            }

            return "$withZoneHoursResult:$withZoneMinutesResult"

        }

        fun convertMaintDate(date: Date?): String {
            date ?: return "-"
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return format.format(date)
        }

        fun isMaintDateOverdue(reportedDate: Date?, nextMaintDate: Date?): Boolean {
            reportedDate ?: return false
            nextMaintDate ?: return false


            return reportedDate.getZeroTimeDate().after(nextMaintDate.getZeroTimeDate())
        }

        private fun Date.getZeroTimeDate(): Date {
            val res: Date
            val calendar = Calendar.getInstance()
            calendar.time = this
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            res = calendar.time
            return res
        }
    }
}