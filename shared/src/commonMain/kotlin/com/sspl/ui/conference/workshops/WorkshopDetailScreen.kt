package com.sspl.ui.conference.workshops

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.ApiStates
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceDetail
import com.sspl.core.models.Session
import com.sspl.platform.formatDateTime
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.theme.orange
import com.sspl.ui.components.AppProgressBar
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.ConferenceAddsOn
import com.sspl.ui.components.Error
import com.sspl.ui.components.SessionBreakDown
import com.sspl.ui.conference.ConferenceDetailViewModel
import com.sspl.utils.FORMAT_DEFAULT_DATE_TIME
import com.sspl.utils.FORMAT_MONTH_YEAR_WEEKDAY
import com.sspl.utils.FORMAT_TIME_12HOUR
import com.sspl.utils.getDayOfMonthSuffixed
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
fun WorkshopDetailScreen(
    navController: NavController,
    viewModel: ConferenceDetailViewModel = koinViewModel()
) {
    val data = navController.previousBackStackEntry?.savedStateHandle?.get<String>("data")
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
            AppProgressBar()
        }

        is ApiStates.Success -> {
            (details as ApiStates.Success<ConferenceDetail>).data?.let {
                WorkshopDetailContent(conference = conference, conferenceDetail = it)
            }
        }
    }
}

@Composable
private fun WorkshopDetailContent(conference: Conference, conferenceDetail: ConferenceDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 12.dp)
    ) {
        item {
            WorkshopDetail(conference, conferenceDetail)
        }

        conferenceDetail.days?.let { listOfDays ->
            itemsIndexed(listOfDays, key = { i, _ -> i }) { i, item ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .let {
                            if (i == listOfDays.size - 1) {
                                it.clip(
                                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp)
                                )
                            } else {
                                it
                            }
                        }
                        .background(boxColor)
                        .padding(all = 12.dp)
                ) {
                    item.breaks?.let { breaks ->
                        SetSessionBreak(breaks)
                    }
                    item.sessions?.let { sessions ->
                        SessionsSchedule(sessions)
                    }
                }
            }
        }

    }
}

@Composable
private fun WorkshopDetail(
    conference: Conference,
    conferenceDetail: ConferenceDetail
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
            )
            .background(columnColor)
            .padding(all = 12.dp)
    ) {
        AppTextSubTitle(
            text = conference.title.orEmpty()
        )
        conference.startDate?.let { date ->
            Spacer(modifier = Modifier.size(4.dp))
            AppTextLabel(
                text = getDayOfMonthSuffixed(date) +
                        " " +
                        formatDateTime(
                            dateTime = date,
                            inFormat = FORMAT_DEFAULT_DATE_TIME,
                            outFormat = FORMAT_MONTH_YEAR_WEEKDAY
                        ),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        val venue = conferenceDetail.days?.firstOrNull()?.sessions?.firstOrNull()?.venue
        venue?.let {
            AppTextBody(
                text = "${it.title},${it.address}"
            )
        }

        Spacer(modifier = Modifier.size(8.dp))
        AppTextBody(
            text = conference.summary.orEmpty()
        )
        if (!conferenceDetail.addOnSets.isNullOrEmpty()) {
            Spacer(modifier = Modifier.size(4.dp))
            ConferenceAddsOn(conferenceDetail.addOnSets)
        }

    }
}

@Composable
private fun SessionsSchedule(sessions: List<Session>) {
    if (sessions.isEmpty()) return
    sessions.forEach { session ->
        Column(modifier = Modifier.fillMaxWidth()) {
            AppTextSubTitle(
                text = session.title,
                color = orange
            )
            if (!session.summary.isNullOrEmpty()) {
                AppTextLabel(text = session.summary)
            }
            if (!session.addOnSets.isNullOrEmpty()) {
                Spacer(modifier = Modifier.size(4.dp))
                ConferenceAddsOn(session.addOnSets)
            }
            if (!session.breakdown.isNullOrEmpty()) {
                SessionBreakDown(session.breakdown)
            }
        }
    }
}

@Composable
private fun SetSessionBreak(breaks: List<Session>) {
    if (breaks.isEmpty()) return
    Column {
        Spacer(modifier = Modifier.size(16.dp))
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
