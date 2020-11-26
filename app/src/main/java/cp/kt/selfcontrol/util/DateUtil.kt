package cp.kt.selfcontrol.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    fun calendar2time(calendar: Calendar): String {
        return SimpleDateFormat("HH:mm:ss").format(calendar.time)
    }

    fun time2Calendar(time: String): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat("HH:mm:ss").parse(time)
        return calendar
    }

    fun time2Date(string: String): Date {
        return SimpleDateFormat("HH:mm:ss").parse(string)
    }

    fun date2Time(date: Date): String {
        return SimpleDateFormat("HH:mm:ss").format(date)
    }

    fun second2Time(second: Long): String {
        val seconds = second % 60
        val minutes = second / 60 % 60
        val hours = second / 3600

        return String.format(
            "%02d:%02d:%02d:",
            hours,
            minutes,
            seconds
        )
    }

    fun time2Second(time: String): Long {
        val temp = time.split(":")
        if (temp.count() == 3) {
            return temp[0].toLong() * 3600 + temp[1].toLong() * 60 + temp[2].toLong()
        }
        return 0
    }
}