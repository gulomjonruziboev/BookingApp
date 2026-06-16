package uz.buron.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.buron.domain.model.CalendarDay
import uz.buron.ui.theme.CalendarAvailable
import uz.buron.ui.theme.CalendarFull
import uz.buron.ui.theme.CalendarPartial
import uz.buron.ui.theme.PurplePrimary
import uz.buron.ui.theme.SessionUnavailable
import uz.buron.ui.theme.TextPrimary
import uz.buron.ui.theme.TextSecondary
import uz.buron.util.Constants
import uz.buron.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DateKeyFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun LocalDate.toCalendarKey(): String = format(DateKeyFormatter)

private fun Map<String, CalendarDay>.dayFor(date: LocalDate): CalendarDay? =
    this[date.toCalendarKey()]

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvailabilityCalendar(
    yearMonth: YearMonth,
    calendar: Map<String, CalendarDay>,
    selectedDate: LocalDate?,
    selectedSessions: Set<String>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate, List<String>) -> Unit,
    onSessionToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cellSize = ((screenWidth - 32.dp) / 7).coerceAtLeast(40.dp)
    val circleSize = (cellSize - 6.dp).coerceAtLeast(34.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(yearMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Oldingi oy")
            }
            Text(
                text = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("uz-UZ")) +
                    " ${yearMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            IconButton(onClick = { onMonthChange(yearMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Keyingi oy")
            }
        }

        val daysOfWeek = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .width(cellSize)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        val firstDay = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val startOffset = firstDay.dayOfWeek.value - 1
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            var dayCounter = 1
            repeat(rows) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellSize)
                ) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        Box(
                            modifier = Modifier
                                .width(cellSize)
                                .height(cellSize),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellIndex >= startOffset && dayCounter <= daysInMonth) {
                                val dayNumber = dayCounter
                                val date = yearMonth.atDay(dayNumber)
                                val dayData = calendar.dayFor(date)
                                val isFull = dayData?.status == "full"
                                val isClickable = dayData != null
                                val isSelected = selectedDate == date
                                val bgColor = when (dayData?.status) {
                                    "available" -> CalendarAvailable
                                    "partial" -> CalendarPartial
                                    "full" -> CalendarFull
                                    else -> CalendarAvailable
                                }
                                val textColor = when {
                                    isSelected && isFull -> Color.White
                                    isSelected -> PurplePrimary
                                    isFull -> Color.White
                                    else -> TextPrimary
                                }
                                val borderColor = when {
                                    isSelected -> PurplePrimary
                                    dayData?.status == "available" || dayData == null ->
                                        TextSecondary.copy(alpha = 0.35f)
                                    else -> TextSecondary.copy(alpha = 0.25f)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(circleSize)
                                        .background(bgColor, CircleShape)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = borderColor,
                                            shape = CircleShape
                                        )
                                        .clickable(enabled = isClickable) {
                                            onDateSelected(date, dayData?.available ?: emptyList())
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = textColor,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                                dayCounter++
                            }
                        }
                    }
                }
            }
        }

        val selectedDay = selectedDate?.let { calendar.dayFor(it) }
        val available = selectedDay?.available ?: emptyList()
        val hasSelectedDate = selectedDate != null

        if (selectedDate != null) {
            Text(
                text = DateUtils.formatDisplayDate(
                    DateUtils.toIsoDateString(selectedDate)
                ),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Constants.SESSION_ORDER.forEach { session ->
                val isAvailable = hasSelectedDate && session in available
                SessionSelectChip(
                    session = session,
                    selected = selectedSessions.contains(session),
                    isAvailable = isAvailable,
                    hasSelectedDate = hasSelectedDate,
                    onClick = { onSessionToggle(session) }
                )
            }
        }
    }
}

@Composable
private fun SessionSelectChip(
    session: String,
    selected: Boolean,
    isAvailable: Boolean,
    hasSelectedDate: Boolean,
    onClick: () -> Unit
) {
    val label = Constants.SESSION_LABELS[session] ?: session
    val backgroundColor = when {
        !hasSelectedDate -> SessionUnavailable.copy(alpha = 0.6f)
        !isAvailable -> SessionUnavailable
        selected -> PurplePrimary.copy(alpha = 0.18f)
        else -> Color.White
    }
    val borderColor = when {
        !hasSelectedDate -> TextSecondary.copy(alpha = 0.2f)
        !isAvailable -> TextSecondary.copy(alpha = 0.35f)
        selected -> PurplePrimary
        else -> TextSecondary.copy(alpha = 0.35f)
    }
    val labelColor = when {
        !hasSelectedDate -> TextSecondary
        !isAvailable -> TextSecondary
        selected -> PurplePrimary
        else -> TextPrimary
    }

    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .clickable(enabled = isAvailable, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor
        )
    }
}
