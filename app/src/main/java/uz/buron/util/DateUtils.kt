package uz.buron.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val uzLocale = Locale.forLanguageTag("uz-UZ")
    private val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", uzLocale)
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dateKeyFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val instant = Instant.parse(isoDate)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            localDate.format(displayFormatter)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatReviewDate(isoDate: String): String {
        return formatDisplayDate(isoDate)
    }

    fun toMonthString(yearMonth: YearMonth): String =
        yearMonth.format(monthFormatter)

    fun toIsoDateString(localDate: LocalDate): String {
        return localDate.atStartOfDay(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
    }

    fun toCalendarKey(localDate: LocalDate): String = localDate.format(dateKeyFormatter)

    fun parseDateKey(key: String): LocalDate? {
        return try {
            LocalDate.parse(key)
        } catch (_: Exception) {
            null
        }
    }
}

object PriceFormatter {
    private val uzLocale = Locale.forLanguageTag("uz-UZ")

    fun format(price: Long): String {
        val formatter = NumberFormat.getNumberInstance(uzLocale)
        return "${formatter.format(price)} so'm"
    }
}

fun sessionLabel(session: String): String =
    Constants.SESSION_LABELS[session] ?: session

fun statusLabel(status: String): String =
    Constants.STATUS_LABELS[status] ?: status
