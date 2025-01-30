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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.ApiStates
import com.sspl.core.models.AddOn
import com.sspl.core.models.AddOnSet
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceDetail
import com.sspl.core.models.Session
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.theme.orange
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.Error
import com.sspl.utils.FORMAT_DAY_MONTH
import com.sspl.utils.FORMAT_DEFAULT_DATE_TIME
import com.sspl.utils.FORMAT_TIME_12HOUR
import com.sspl.utils.getJoinedDateTime
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

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
    when (details) {
        is ApiStates.Failure -> {
            Error(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                error = (details as ApiStates.Failure).error
            )
        }

        ApiStates.Loading -> {
            ProgressBar()
        }

        is ApiStates.Success -> {
            (details as ApiStates.Success<ConferenceDetail>).data?.let {
                ConferenceDetailContent(conference = conference, conferenceDetail = it)
            }
        }
    }
}

@Composable
private fun ConferenceDetailContent(conference: Conference, conferenceDetail: ConferenceDetail) {
    Column(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
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
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp)
        ) {
            AppTextSubTitle(
                text = conference.title.orEmpty()
            )
            Spacer(modifier = Modifier.size(4.dp))
            AppTextLabel(
                text = getJoinedDateTime(
                    dateTime1 = conference.startDate.orEmpty(),
                    dateTime2 = conference.endDate.orEmpty(),
                    inFormat = FORMAT_DEFAULT_DATE_TIME,
                    outFormat = FORMAT_DAY_MONTH
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            AppTextBody(
                text = conference.summary.orEmpty()
            )

            ConferenceAddOns(conferenceDetail.addOnSets)
        }

        conferenceDetail.days?.let { listOfDays ->
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
                    item.breaks?.let { breaks ->
                        Spacer(modifier = Modifier.size(12.dp))
                        SetSessionBreak(breaks)
                    }
                    item.sessions?.let { sessions ->
                        SessionsSchedule(sessions, listOfDays.size != i + 1)
                    }
                }
            }
        }

    }
}

@Composable
private fun ProgressBar() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SessionsSchedule(sessions: List<Session>, showDivider: Boolean) {
    val groupedSessions = sessions.groupBy {
        getJoinedDateTime(
            it.breakdown?.firstOrNull()?.startsAt ?: "",
            it.breakdown?.lastOrNull()?.endsAt ?: "",
            outFormat = FORMAT_TIME_12HOUR
        )
    }//08-09

    groupedSessions.keys.forEachIndexed { index, key ->
        Column {
            groupedSessions[key]?.let { sessions ->
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppTextSubTitle(
                        text = "Time",
                        color = orange
                    )
                    AppTextSubTitle(
                        text = key,
                        color = orange
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Sessions(sessions)
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
private fun Sessions(items: List<Session>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val size = items.size
        itemsIndexed(items, key = { i, _ -> i }) { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.padding(end = 8.dp).fillMaxWidth(1F)) {
                    AppTextLabel(text = item.title)
                    val uniqueSpeakers = item.breakdown
                        ?.filter { !it.isBreak } // Filter out breaks
                        ?.mapNotNull {
                            it.speakers.firstOrNull()?.fullName()
                        } // Extract speaker names
                        ?.distinct() // Keep only unique speakers
                        ?.take(3) // Get the first 3 unique speakers
                        .orEmpty()
                    val speakers = uniqueSpeakers.joinToString("\n").trim()
                    AppTextBody(text = speakers)
                    AppTextBody(text = item.summary ?: "")
                    AppTextBody(text = item.venue?.title ?: "", color = orange)
                    Spacer(modifier = Modifier.size(4.dp))
                    AppTextSubTitle(
                        text = "MORE DETAIL",
                        modifier = Modifier.align(alignment = Alignment.End),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 10.sp,
                        isAllCapsStyled = true,
                        fontSizeCap = 14.sp
                    )
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

@Composable
private fun SetSessionBreak(breaks: List<Session>) {
    Column {
        Spacer(modifier = Modifier.size(4.dp))
        AppTextSubTitle(
            text = "Break",
            color = orange
        )
        breaks.forEach {
            Spacer(modifier = Modifier.size(4.dp))
            val time = getJoinedDateTime(
                dateTime1 = it.startsAt.orEmpty(),
                dateTime2 = it.endsAt.orEmpty(),
                inFormat = FORMAT_DEFAULT_DATE_TIME,
                outFormat = FORMAT_TIME_12HOUR
            )
            AppTextLabel(text = it.title)
            AppTextBody(text = time)
            it.summary?.let { summary ->
                AppTextBody(text = summary)
            }
        }
    }
}

@Composable
fun ConferenceAddOns(addOnSet: List<AddOnSet>?) {
    addOnSet?.let { addOnSets ->
        LazyColumn {
            addOnSets.forEach { addOnSet ->
                item {
                    AddOnSetItem(addOnSet)
                }
            }
        }
    }
}

@Composable
fun AddOnSetItem(addOnSet: AddOnSet) {
    Column {
        Spacer(modifier = Modifier.size(4.dp))
        AppTextSubTitle(
            text = addOnSet.title ?: "",
            color = orange
        )
        addOnSet.addOns?.forEach { addOn ->
            Spacer(modifier = Modifier.size(4.dp))
            AddOnItem(addOn)
        }
    }
}

@Composable
fun AddOnItem(addOn: AddOn) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.Start) {
            AppTextLabel(text = addOn.title ?: "")
            addOn.subtitle?.let { subtitle ->
                AppTextBody(text = subtitle)
            }
        }
    }
}