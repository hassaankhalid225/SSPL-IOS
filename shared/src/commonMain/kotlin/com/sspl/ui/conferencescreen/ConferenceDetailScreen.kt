package com.sspl.ui.conferencescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.ApiStates
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceDetails
import com.sspl.core.models.Day
import com.sspl.core.models.Session
import com.sspl.platform.formatDateTime
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.theme.orange
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.utils.FORMAT_DAY_MONTH
import com.sspl.utils.FORMAT_DEFAULT_DATE_TIME
import com.sspl.utils.FORMAT_TIME_12HOUR
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import kotlin.test.todo

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 05/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun ConferenceDetailScreen(
    navController: NavController,
    viewModel: ConferenceDetailViewModel = koinViewModel()
) {
    val data = navController.previousBackStackEntry?.savedStateHandle?.get<String>(
        "data"
    )
    if (data == null) return
    val conference: Conference = Json.decodeFromString<Conference>(data)

    LaunchedEffect(Unit) {
        viewModel.getConferenceById(conference.id)
    }
    val details by viewModel.conferences.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .clip(
                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
            )
            .background(boxColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth().background(columnColor)
                .clip(
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
                )
                .padding(all = 12.dp)
        ) {
            AppTextSubTitle(
                text = conference.title
            )
            Spacer(modifier = Modifier.size(4.dp))
            AppTextLabel(
                text = "${
                    formatDateTime(
                        dateTime = conference.startDate,
                        inFormat = FORMAT_DEFAULT_DATE_TIME,
                        outFormat = FORMAT_DAY_MONTH
                    )
                } \u2014 ${
                    formatDateTime(
                        dateTime = conference.endDate, inFormat = FORMAT_DEFAULT_DATE_TIME,
                        outFormat = FORMAT_DAY_MONTH
                    )
                }"
            )
            Spacer(modifier = Modifier.size(8.dp))
            AppTextBody(
                text = conference.summary
            )
        }
        when (details) {
            is ApiStates.Failure -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AppTextBody(
                        text = (details as ApiStates.Failure).error.message ?: "An error occurred",
                        color = Color.Red,
                    )
                }
            }

            ApiStates.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                    )
                }
            }
            is ApiStates.Success -> {
                (details as ApiStates.Success<ConferenceDetails>).data?.days.let { listOfDays ->
                    if (listOfDays != null) {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                bottom = 12.dp
                            )
                        ) {
                            itemsIndexed(listOfDays, key = { i, _ -> i }) { i, item ->
                                Spacer(modifier = Modifier.size(12.dp))
                                AppTextSubTitle(
                                    text = "Day 0${i + 1}",
                                    color = orange
                                )
                                Schedule(item, listOfDays.size != i + 1)
                            }
                        }
                    }
                }
            }
        }

    }
}
@Composable
private fun Schedule(item: Day, showDivider: Boolean) {
    val groupedSessions = item.sessions.groupBy {
        formatTimeRange(it.startsAt, it.endsAt)
    }
    val groupedBreaks = item.breaks.groupBy {
        formatTimeRange(it.startsAt, it.endsAt)
    }

    groupedSessions.keys.forEachIndexed { index, key ->
        Column {
            Spacer(modifier = Modifier.size(8.dp))

            groupedBreaks[key]?.let { breaks ->
                groupedSessions[key]?.let { sessions ->
                    val breakInfo = breaks.firstOrNull()
                    val sessionInfo = sessions.firstOrNull()

                    sessionInfo?.let {
                        TimeRangeText(it.startsAt, it.endsAt, isBold = true)
                    }
                    breakInfo?.summary?.let { summary ->
                        AppTextBody(text = summary)
                    }
                    breakInfo?.let {
                        TimeRangeText(it.startsAt, it.endsAt)
                    }
                    Sessions(sessions)
                }
            }

            if (showDivider || groupedSessions.size != index + 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    thickness = 2.dp,
                    color = columnColor
                )
            }
        }
    }
}

@Composable
private fun TimeRangeText(startTime: String, endTime: String,isBold:Boolean=false) {
    if (isBold){
        AppTextLabel(
            text = formatTimeRange(
                dateTimeStart = startTime,
                dateTimeEnd = endTime,
                inFormat = FORMAT_DEFAULT_DATE_TIME,
                outFormat = FORMAT_TIME_12HOUR

            ))
    }else{
    AppTextBody(
        text = formatTimeRange(
            dateTimeStart = startTime,
            dateTimeEnd = endTime,
            inFormat = FORMAT_DEFAULT_DATE_TIME,
            outFormat = FORMAT_TIME_12HOUR
        )
    )
}}

private fun formatTimeRange(
    dateTimeStart: String,
    dateTimeEnd: String,
    inFormat: String = "dd:MM:yyyy HH:mm:ss",
    outFormat: String = "HH:mm"
): String {
    return "${formatDateTime(dateTimeStart, inFormat, outFormat)} \u2014 ${formatDateTime(dateTimeEnd, inFormat, outFormat)}"
}
@Composable
private fun Sessions(items: List<Session>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val size = items.size
        itemsIndexed(items, key = { i, _ -> i }) { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    AppTextLabel(text = item.title)
                    AppTextBody(text = item.speakers.joinToString(separator = "\n") { it.prefix + " "+ it.firstName+" " + it.lastName })
                    AppTextBody(text = item.summary ?: "")
                    AppTextBody(text = item.venue?.title ?: "", color = orange)
                }
                if (size != index + 1) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(40.dp),
                        thickness = 2.dp,
                        color = columnColor
                    )
                }
            }
        }
    }
}