package com.sspl.ui.conferencescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.models.Conference
import com.sspl.platform.formatDateTime
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.theme.dividerColor
import com.sspl.theme.orange
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.Error
import com.sspl.utils.FORMAT_DAY_MONTH
import com.sspl.utils.FORMAT_DEFAULT_DATE_TIME
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
fun ConferenceScreen(
    navController: NavController,
    viewModel: ConferenceViewModel = koinViewModel()
) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val type = savedStateHandle?.get<String>("type")
    if (type == null) return
    println("Type :$type")
    val uiState by viewModel.conferences.collectAsStateWithLifecycle()

    when (uiState) {
        is ApiStates.Loading -> {
            LoadingScreen()
        }

        is ApiStates.Failure -> {
            Error(
                modifier = Modifier.fillMaxSize(),
                error = (uiState as ApiStates.Failure).error
            )
        }

        is ApiStates.Success -> {
            (uiState as ApiStates.Success<List<Conference>>).data?.let {
                ConferenceList(
                    conferences = it,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


@Composable
fun ConferenceList(
    conferences: List<Conference>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp)
    ) {
        itemsIndexed(conferences, key = { i, _ -> i }) { i, item ->
            ConferenceItem(
                item = item,
                position = i,
                showDivider = i != conferences.size - 1
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "data",
                    Json.encodeToString(item)
                )
                navController.navigate(route = Screen.ConferenceDetailScreen.route)
            }
        }
    }
}

@Composable
fun ConferenceItem(
    item: Conference, position: Int, showDivider: Boolean = true, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().let {
            if (position == 0) {
                it.then(
                    Modifier.clip(
                        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
                    )
                )
            } else if (!showDivider) {
                Modifier.clip(
                    shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)
                )
            } else {
                Modifier.clip(
                    shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp)
                )
            }
        }.background(
            color = columnColor
        ).clickable {
            onClick.invoke()
        }) {
        Column(
            modifier = Modifier.fillMaxWidth(.22F)
        ) {
            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(
                        "${
                            formatDateTime(
                                dateTime = item.startDate ?: "",
                                inFormat = FORMAT_DEFAULT_DATE_TIME,
                                outFormat = FORMAT_DAY_MONTH
                            ).uppercase()
                        }\n"
                    )
                }
                withStyle(
                    style = SpanStyle(
                        color = orange, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                ) {
                    append("⋮\n")
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(
                        formatDateTime(
                            dateTime = item.endDate ?: "",
                            inFormat = FORMAT_DEFAULT_DATE_TIME,
                            outFormat = FORMAT_DAY_MONTH
                        ).uppercase()
                    )
                }
            }
            Text(
                text = annotatedString,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(1F).clip(
                shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp)
            ).background(color = boxColor)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(all = 8.dp)
            ) {
                AppTextSubTitle(
                    text = item.title ?: ""
                )
                Spacer(modifier = Modifier.size(8.dp))

                AppTextBody(
                    text = item.summary ?: ""
                )
                Spacer(modifier = Modifier.size(16.dp))
                AppTextLabel(
                    text = "MORE DETAILS",
                    modifier = Modifier.padding(top = 8.dp).align(alignment = Alignment.End)
                )
            }
            if (showDivider) {
                Spacer(
                    modifier = Modifier.fillMaxWidth().height(2.dp).padding(end = 12.dp)
                        .background(color = dividerColor)
                )
            }
        }
    }
}