package com.sspl.ui.conference.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceDetail
import com.sspl.core.models.Session
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.theme.columnColorLight
import com.sspl.theme.orange
import com.sspl.ui.components.AppProgressBar
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.ConferenceAddsOn
import com.sspl.ui.components.Error
import com.sspl.ui.conference.ConferenceDetailViewModel
import com.sspl.utils.FORMAT_DAY_MONTH
import com.sspl.utils.FORMAT_DEFAULT_DATE_TIME
import com.sspl.utils.FORMAT_TIME_12HOUR
import com.sspl.utils.VERTICAL_DOTS
import com.sspl.utils.getJoinedDateTime
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 05/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun ProgramDetailScreen(
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
                ConferenceDetailContent(
                    navController = navController,
                    conference = conference,
                    conferenceDetail = it
                )
            }
        }
    }
}

@Composable
private fun ConferenceDetailContent(
    navController: NavController,
    conference: Conference,
    conferenceDetail: ConferenceDetail
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 12.dp)
    ) {
        item {
            ConferenceDetail(conference, conferenceDetail)
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
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 2.dp,
                        color = columnColor
                    )
                    AppTextSubTitle(
                        modifier = Modifier.fillMaxWidth().background(color = columnColorLight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        text = "Day 0${i + 1}",
                        color = orange
                    )
                    scope.launch {

                    }
                    val sessionWithBreaks = (item.breaks.orEmpty() + item.sessions.orEmpty())
                        .sortedBy {
                            if (it.isBreak) it.startsAt
                            else it.breakdown?.firstOrNull()?.startsAt
                        }
                    if (sessionWithBreaks.isNotEmpty()) {
                        SessionsSchedule(sessionWithBreaks) { session ->
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "data",
                                Json.encodeToString(session)
                            )
                            navController.navigate(route = Screen.SessionDetailScreen.route)
                        }
                    }
                }

                if (i != listOfDays.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 2.dp,
                        color = columnColor
                    )
                }
            }
        }
    }
}


@Composable
private fun ConferenceDetail(
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
            text = conference.title.orEmpty().trim()
        )
        Spacer(modifier = Modifier.size(4.dp))
        AppTextLabel(
            text = getJoinedDateTime(
                dateTime1 = conference.startDate.orEmpty(),
                dateTime2 = conference.endDate.orEmpty(),
                inFormat = FORMAT_DEFAULT_DATE_TIME,
                outFormat = FORMAT_DAY_MONTH
            ),
            color = MaterialTheme.colorScheme.tertiary,
        )

        Spacer(modifier = Modifier.size(8.dp))
        AppTextBody(
            text = conference.summary.orEmpty().trim()
        )
        if (!conferenceDetail.addOnSets.isNullOrEmpty()) {
            Spacer(modifier = Modifier.size(4.dp))
            ConferenceAddsOn(conferenceDetail.addOnSets)
        }
    }
}

@Composable
private fun ColumnScope.SessionsSchedule(sessions: List<Session>, onClick: (Session) -> Unit) {
    if (sessions.isEmpty()) return
    sessions.forEachIndexed { index, session ->
        SessionItem(session, sessions.size != index + 1, onClick)
    }
}

@Composable
private fun ColumnScope.SessionItem(
    session: Session,
    showDivider: Boolean,
    onClick: (Session) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(!session.isBreak) {
                onClick.invoke(session)
            }
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(.22F).defaultMinSize(minHeight = 68.dp)
        ) {
            val startAt = if (session.isBreak) {
                session.startsAt
            } else {
                session.breakdown?.firstOrNull()?.startsAt
            }
            val endAt = if (session.isBreak) {
                session.endsAt
            } else {
                session.breakdown?.firstOrNull()?.endsAt
            }
            val time = getJoinedDateTime(
                startAt.orEmpty(),
                endAt.orEmpty(),
                outFormat = FORMAT_TIME_12HOUR,
                divider = VERTICAL_DOTS,
                separator = "\n"
            )
            AppTextLabel(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary,
                text = time,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(1F)
                .defaultMinSize(minHeight = 68.dp)
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)

        ) {
            if (!session.addOnSets.isNullOrEmpty()) {
                ConferenceAddsOn(session.addOnSets)
                Spacer(modifier = Modifier.size(4.dp))
            }
            AppTextLabel(
                text = session.title.trim()
            )
            Spacer(modifier = Modifier.size(4.dp))
            session.summary?.let { AppTextBody(text = session.summary.trim()) }
            Spacer(modifier = Modifier.size(8.dp))
            session.venue?.let { venue ->
                venue.title?.let {
                    AppTextBody(text = venue.title.trim(), color = orange)
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }

        }
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(.78F)
                .padding(start = 12.dp)
                .align(alignment = Alignment.End),
            thickness = 1.dp,
            color = columnColor
        )
    }
}