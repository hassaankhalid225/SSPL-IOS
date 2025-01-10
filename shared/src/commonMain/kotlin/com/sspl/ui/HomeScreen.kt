package com.sspl.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.resources.Res
import com.sspl.resources.ad_sspl
import com.sspl.ui.components.AppHorizontalPager
import com.sspl.ui.components.BetterModalBottomSheet
import com.sspl.ui.conferencescreen.ConferenceScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var showSheet by rememberSaveable { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
//    LazyColumn(modifier = Modifier.fillMaxSize()) {
//        item {
//            AppHorizontalPager(
//                listOf(
//                    Res.drawable.ad_sspl,
//                    Res.drawable.ad_sspl,
//                    Res.drawable.ad_sspl
//                )
//            ) { modifier, item, page ->
//                Image(
//                    modifier = modifier.then(Modifier.fillMaxHeight(.1F)),
//                    contentScale = ContentScale.FillWidth,
//                    painter = painterResource(item), contentDescription = ""
//                )
//            }
//        }
//        items(12) {
//            //Here we will show posts
//            Image(
//                modifier = Modifier.fillMaxWidth().height(80.dp),
//                contentScale = ContentScale.FillWidth,
//                painter = painterResource(Res.drawable.ad_sspl), contentDescription = ""
//            )
//        }
//    }
    ConferenceScreen(navController)
    AnimatedVisibility(visible = showSheet) {
        BetterModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
//            confirmValueChange = {
//                //if (condition) true else false
//                false
//            }
            ),
            onDismissRequest = {
                scope.launch { showSheet = false }
            }
        ) {
            Image(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(.9F).padding(all = 12.dp),
                contentScale = ContentScale.FillBounds,
                painter = painterResource(Res.drawable.ad_sspl),
                contentDescription = ""
            )
        }
    }
}
