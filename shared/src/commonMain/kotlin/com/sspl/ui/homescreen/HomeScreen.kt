package com.sspl.ui.homescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.platform.shared.UrlOpener
import com.sspl.resources.Res
import com.sspl.resources.ad_sspl
import com.sspl.resources.banner_five
import com.sspl.resources.banner_four
import com.sspl.resources.banner_one
import com.sspl.resources.banner_six
import com.sspl.resources.banner_three
import com.sspl.resources.banner_two
import com.sspl.resources.exhibition
import com.sspl.resources.forum
import com.sspl.resources.ic_exhibition
import com.sspl.resources.ic_forum
import com.sspl.resources.ic_notifications
import com.sspl.resources.ic_scientific_program
import com.sspl.resources.ic_user
import com.sspl.resources.ic_workshop
import com.sspl.resources.notifications
import com.sspl.resources.profile
import com.sspl.resources.scientific_programs
import com.sspl.resources.workshops
import com.sspl.theme.boxColor
import com.sspl.theme.columnColor
import com.sspl.theme.platformConfig
import com.sspl.ui.components.AppHorizontalPager
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.BetterModalBottomSheet
import com.sspl.utils.IconResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeScreenViewModel = koinViewModel()) {
    var showSheet = homeViewModel.isAdShown.collectAsState()
    val urlOpener = koinInject<UrlOpener>()
    val scope = rememberCoroutineScope()
    val data = listOf(
        Item(
            title = stringResource(Res.string.scientific_programs),
            icon = IconResource.fromDrawableResource(Res.drawable.ic_scientific_program),
            toScreen = Screen.ScientificProgramsScreen
        ),
        Item(
            title = stringResource(Res.string.workshops),
            icon = IconResource.fromDrawableResource(Res.drawable.ic_workshop),
            toScreen = Screen.WorkshopsScreen
        ),
        Item(
            title = stringResource(Res.string.forum),
            icon = IconResource.fromDrawableResource(Res.drawable.ic_forum),
            toScreen = Screen.ForumScreen
        ),
        Item(
            title = stringResource(Res.string.profile),
            icon = IconResource.fromDrawableResource(Res.drawable.ic_user),
            toScreen = Screen.ProfileScreen
        ),
        Item(
            title = stringResource(Res.string.notifications),
            icon = IconResource.fromDrawableResource(Res.drawable.ic_notifications),
            toScreen = Screen.SessionScreen
        ),
        Item(
            title = stringResource(Res.string.exhibition),
            icon = IconResource.fromDrawableResource(Res.drawable.ic_exhibition),
            toScreen = Screen.ProfileScreen
        ),
    )
    val bannerImages = listOf(
        Res.drawable.banner_one,
        Res.drawable.banner_two,
        Res.drawable.banner_three,
        Res.drawable.banner_four,
        Res.drawable.banner_five,
        Res.drawable.banner_six
    )
    val pagerState = rememberPagerState(pageCount = {
        bannerImages.size
    })
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            val currentPage = pagerState.currentPage
            scope.launch {
                if (currentPage + 1 == bannerImages.size) {
                    pagerState.animateScrollToPage(0)
                } else {
                    pagerState.animateScrollToPage(currentPage + 1)
                }
            }
        }
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            AppHorizontalPager(
                list = bannerImages,
                pagerState = pagerState,
                showIndicator = false
            ) { modifier, item, page ->
                Image(
                    modifier = modifier.then(
                        Modifier.fillMaxWidth().clip(
                            shape = RoundedCornerShape(8)
                        )
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
                        if (index == 5) {
                            urlOpener.openUrl("https://www.google.com/")
                        } else {
                            navController.navigate(it.toScreen.route)
                        }
                    }
                }
            }

        }
    }

    AnimatedVisibility(visible = showSheet.value) {
        BetterModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
//            confirmValueChange = {
//                //if (condition) true else false
//                false
//            }
            ),
            onDismissRequest = {
                scope.launch { homeViewModel.updateAdShown(false) }
            }
        ) {

            val screenHeight = MaterialTheme.platformConfig.screenHeight
            val maxHeight = screenHeight * 0.85f
            Image(
                modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight)
                    .padding(horizontal = 12.dp),
                contentScale = ContentScale.FillWidth,
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
