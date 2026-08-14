package org.eos.mynoti.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.eos.mynoti.R
import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.ui.components.AppIcon
import org.eos.mynoti.ui.components.ImportanceBadge
import org.eos.mynoti.ui.components.TypeBadge
import org.eos.mynoti.ui.components.accentColor
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.util.toReceivedTimeLabel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val weekdayLabels = listOf("일", "월", "화", "수", "목", "금", "토")
private val monthTitleFormatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
private val selectedDateFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
private val CalendarEventCardShape = RoundedCornerShape(16.dp)

@Composable
fun CalendarMonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMonthClick: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pickMonthDescription = stringResource(R.string.calendar_pick_month)
    val todayDescription = stringResource(R.string.calendar_today_go)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = stringResource(R.string.calendar_previous_month)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = MyNotiDimens.minTouchTarget)
                .clip(RoundedCornerShape(MyNotiDimens.iconRadius))
                .clickable(onClick = onMonthClick)
                .semantics {
                    role = Role.Button
                    contentDescription = "${month.format(monthTitleFormatter)}, $pickMonthDescription"
                }
                .padding(horizontal = MyNotiDimens.spaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = month.format(monthTitleFormatter),
                style = MyNotiTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(start = MyNotiDimens.spaceXs)
                    .size(MyNotiDimens.spaceXl)
            )
        }
        TextButton(
            onClick = onToday,
            modifier = Modifier.semantics {
                contentDescription = todayDescription
            }
        ) {
            Text(text = stringResource(R.string.calendar_today))
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = stringResource(R.string.calendar_next_month)
            )
        }
    }
}

@Composable
fun CalendarAddEventButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            modifier = Modifier.size(MyNotiDimens.spaceLg)
        )
        Spacer(modifier = Modifier.width(MyNotiDimens.spaceXs))
        Text(text = stringResource(R.string.calendar_add_event))
    }
}

@Composable
fun CalendarWeekdayHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        weekdayLabels.forEach { label ->
            Text(
                text = label,
                style = MyNotiTextStyles.caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    typesByDate: (LocalDate) -> List<NotificationType>,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = month.calendarDays()
    Column(modifier = modifier.fillMaxWidth()) {
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    CalendarDayCell(
                        date = date,
                        selected = date == selectedDate,
                        isToday = date == today,
                        types = date?.let(typesByDate).orEmpty(),
                        onClick = { date?.let(onSelectDate) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    selected: Boolean,
    isToday: Boolean,
    types: List<NotificationType>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayLabel = stringResource(R.string.calendar_today)
    val selectedLabel = stringResource(R.string.calendar_day_selected)
    val dayDescription = date?.let { cellDate ->
        buildString {
            append(cellDate.format(selectedDateFormatter))
            if (isToday) {
                append(", ")
                append(todayLabel)
            }
            if (selected) {
                append(", ")
                append(selectedLabel)
            }
        }
    }
    Column(
        modifier = modifier
            .semantics {
                role = Role.Button
                if (dayDescription != null) {
                    contentDescription = dayDescription
                }
            }
            .clickable(enabled = date != null, onClick = onClick)
            .padding(vertical = MyNotiDimens.spaceXs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val dayLabel = date?.dayOfMonth?.toString().orEmpty()
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .then(
                    when {
                        selected -> Modifier.background(MaterialTheme.colorScheme.primary)
                        isToday -> Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        else -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayLabel,
                style = MyNotiTextStyles.notificationTitle,
                color = when {
                    date == null -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0f)
                    selected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )
        }
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(6.dp)
        ) {
            types.take(3).forEach { type ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(type.accentColor())
                )
            }
        }
    }
}

@Composable
fun CalendarSelectedDateTitle(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Text(
        text = date.format(selectedDateFormatter),
        style = MyNotiTextStyles.sectionTitle,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun CalendarEventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CalendarEventCardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(MyNotiDimens.cardPadding),
            verticalAlignment = Alignment.Top
        ) {
            AppIcon(
                appPackageName = event.appPackageName,
                size = MyNotiDimens.appIcon,
                contentDescription = event.appName
            )
            Spacer(modifier = Modifier.width(MyNotiDimens.spaceMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.appName,
                    style = MyNotiTextStyles.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.title,
                    style = MyNotiTextStyles.notificationTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
                Text(
                    text = event.eventAt.toReceivedTimeLabel(),
                    style = MyNotiTextStyles.notificationSummary,
                    color = MaterialTheme.colorScheme.onSurface
                )
                event.location?.let { location ->
                    Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = stringResource(R.string.cd_calendar_location),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(MyNotiDimens.spaceMd)
                        )
                        Spacer(modifier = Modifier.width(MyNotiDimens.spaceXs))
                        Text(
                            text = location,
                            style = MyNotiTextStyles.notificationSummary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeBadge(type = event.type)
                    if (event.isImportant) {
                        ImportanceBadge()
                    }
                }
            }
            event.receivedAt?.let { receivedAt ->
                Spacer(modifier = Modifier.width(MyNotiDimens.spaceSm))
                Text(
                    text = stringResource(
                        R.string.calendar_received,
                        receivedAt.toReceivedTimeLabel()
                    ),
                    style = MyNotiTextStyles.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
