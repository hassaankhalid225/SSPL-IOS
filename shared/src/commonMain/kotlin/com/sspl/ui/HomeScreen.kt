package com.sspl.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceType
import com.sspl.resources.Res
import com.sspl.resources.ad_sspl
import com.sspl.resources.ic_forum
import com.sspl.resources.ic_notifications
import com.sspl.resources.ic_scientific_program
import com.sspl.resources.ic_user
import com.sspl.resources.ic_workshop
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.ui.components.AppHorizontalPager
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.BetterModalBottomSheet
import com.sspl.utils.IconResource
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val data = listOf(
        Item(
            title = "Scientific Programs",
            icon = IconResource.fromDrawableResource(Res.drawable.ic_scientific_program),
            toScreen = Screen.ConferenceScreen
        ),
        Item(
            title = "Workshops",
            icon = IconResource.fromDrawableResource(Res.drawable.ic_workshop),
            toScreen = Screen.ConferenceScreen
        ),
        Item(
            title = "Forum",
            icon = IconResource.fromDrawableResource(Res.drawable.ic_forum),
            toScreen = Screen.ForumScreen
        ),
        Item(
            title = "Profile",
            icon = IconResource.fromDrawableResource(Res.drawable.ic_user),
            toScreen = Screen.ProfileScreen
        ),
        Item(
            title = "Notifications",
            icon = IconResource.fromDrawableResource(Res.drawable.ic_notifications),
            toScreen = Screen.ProfileScreen
        ),
    )
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            AppHorizontalPager(
                list = listOf(
                    Res.drawable.ad_sspl,
                    Res.drawable.ad_sspl,
                    Res.drawable.ad_sspl
                ),
                showIndicator = false
            ) { modifier, item, page ->
                Image(
                    modifier = modifier.then(Modifier.height(100.dp))
                        .clip(
                            shape = RoundedCornerShape(8)
                        ),
                    contentScale = ContentScale.FillWidth,
                    painter = painterResource(item), contentDescription = ""
                )
            }
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 3 columns
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(min = 200.dp, max = 1200.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                itemsIndexed(data, key = { i, _ -> i }) { index, it ->
                    SquareItem(it) {
                        if (index == 0) {
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "type",
                                ConferenceType.PROGRAM.name
                            )
                        } else if (index == 1) {
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "type",
                                ConferenceType.WORKSHOP.name
                            )
                        }
                        navController.navigate(it.toScreen.route)
                    }
                }
            }

        }
    }

//    Box(modifier = Modifier.fillMaxSize().background(color = Color.Red))

//    ConferenceScreen(navController)
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

@Composable
private fun SquareItem(item: Item, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1F)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8),
        colors = CardDefaults.cardColors().copy(
            containerColor = boxColor.copy(alpha = .3F)
        ),
        border = BorderStroke(width = 1.dp, color = columnColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().clickable {
                onItemClick.invoke()
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(.35F),
                painter = item.icon.icon(),
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.secondary
            )
            AppTextSubTitle(
                text = item.title,
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 12.dp)
                    .align(alignment = Alignment.BottomCenter),
                isAllCapsStyled = true,
                fontSizeCap = 15.sp
            )
        }
    }
}

private data class Item(val title: String, val icon: IconResource, val toScreen: Screen)
