package co.ltlabs.ltmechanic.util

import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object DateTimeUtil {

    fun parseWithTimeZone(date: Date?): String {
        date ?: return "-"
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    fun parseWithTimeZone(stringDateTime: String): String {
        val parsedDateTime = DateUtil.getDateTimeWithTimeZone(stringDateTime)
        return getTime(parsedDateTime)
    }

    fun parseWithDateTimeZone(stringDateTime: String): ArrayList<String> {
        val parsedDateTime = DateUtil.getDateTimeWithTimeZone(stringDateTime)
        val date = getDate(parsedDateTime)
        val time = getTime(parsedDateTime)
        val formattedTime = displayInTwelveFormat(time)
        return arrayListOf(date, formattedTime)
    }

    private fun getDate(dateTime: DateTime): String {
        return DateUtil.getDate(dateTime)
    }

    private fun getTime(dateTime: DateTime): String {
        return DateUtil.getTime(dateTime)
    }

    private fun displayInTwelveFormat(stringTime: String): String {
        val oldFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val oldTimeFormat = oldFormat.parse(stringTime)
        val newFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return newFormat.format(oldTimeFormat ?: stringTime)
    }

}