package com.sspl.ui.conferencescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.platform.formatDateTime
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.theme.orange
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import kotlinx.serialization.json.Json

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 05/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun ConferenceDetailScreen(navController: NavController) {}
//    val data = navController.previousBackStackEntry?.savedStateHandle?.get<String>(
//        "data"
//    )
//    if (data == null) return
//
//    val conference: Conference = Json.decodeFromString<Conference>(data)
//    Column(
//        modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top)
//            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
//            .clip(
//                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
//            )
//            .background(boxColor)
//
//    ) {
//        Column(
//            modifier = Modifier
//                .background(columnColor)
//                .clip(
//                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
//                )
//                .padding(all = 12.dp)
//
//        ) {
//            AppTextSubTitle(
//                text = conference.title
//            )
//            Spacer(modifier = Modifier.size(4.dp))
//            AppTextLabel(
//                text = "${
//                    formatDateTime(
//                        dateTime = conference.startAt,
//                        inFormat = "dd/MM/yyyy",
//                        outFormat = "dd MMM"
//                    )
//                } \u2014 ${
//                    formatDateTime(
//                        dateTime = conference.endAt,
//                        inFormat = "dd/MM/yyyy",
//                        outFormat = "dd MMM"
//                    )
//                }"
//            )
//            Spacer(modifier = Modifier.size(8.dp))
//
//            AppTextBody(
//                text = conference.shortMessage
//            )
//
//        }
//        conference.schedule.let {
//            LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
//                itemsIndexed(it, key = { i, _ -> i }) { i, item ->
//                    Spacer(modifier = Modifier.size(12.dp))
//                    AppTextSubTitle(
//                        text = "Day 0${i + 1}",
//                        color = orange
//                    )
//                    Schedule(item, it.size != i + 1)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun Schedule(item: Schedule, showDivider: Boolean) {
//    val grouped = item.sessions.groupBy {
//        "${
//            formatDateTime(
//                dateTime = it.start,
//                inFormat = "dd:MM:yyyy hh:mm:ss",
//                outFormat = "HH:mm"
//            )
//        } \u2014 ${
//            formatDateTime(
//                dateTime = it.end,
//                inFormat = "dd:MM:yyyy HH:mm:ss",
//                outFormat = "HH:mm"
//            )
//        }"
//    }
//
//    grouped.keys.forEachIndexed { i, key ->
//        Column {
//            Spacer(modifier = Modifier.size(8.dp))
//            AppTextLabel(
//                text = key
//            )
//
//            grouped[key]?.let {
//                val session = it.firstOrNull()
//                session?.breakMessage?.let {
//                    AppTextBody(
//                        text = it
//                    )
//                }
//                session?.start?.let {
//                    AppTextBody(
//                        text = "${
//                            formatDateTime(
//                                dateTime = session.start,
//                                inFormat = "dd:MM:yyyy hh:mm:ss",
//                                outFormat = "HH:mm"
//                            )
//                        } \u2014 ${
//                            formatDateTime(
//                                dateTime = session.end,
//                                inFormat = "dd:MM:yyyy HH:mm:ss",
//                                outFormat = "HH:mm"
//                            )
//                        }"
//                    )
//                }
//
//                Sessions(it)
//            }
//
//            if (showDivider || grouped.size != i + 1) {
//                HorizontalDivider(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 8.dp),
//                    thickness = 2.dp,
//                    color = columnColor
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun Sessions(items: List<Session>) {
//    LazyRow(
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        val size = items.size
//        itemsIndexed(items, key = { i, _ -> i }) { index, item ->
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Column(modifier = Modifier.padding(end = 8.dp)) {
//                    AppTextLabel(text = item.title)
//                    AppTextBody(text = item.speakers.joinToString (separator = "\n"){ it })
//                    AppTextBody(text = item.message)
//                    AppTextBody(text = item.hall, color = orange)
//
//                }
//                if (size != index + 1) {
//                    VerticalDivider(
//                        modifier = Modifier
//                            .height(40.dp),
//                        thickness = 2.dp,
//                        color = columnColor
//                    )
//                }
//            }
//        }
//    }
//}