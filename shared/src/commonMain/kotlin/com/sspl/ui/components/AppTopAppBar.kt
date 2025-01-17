package com.sspl.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.Screen
import org.jetbrains.compose.resources.stringResource

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 13/08/2024.
 * se.muhammadimran@gmail.com
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(
    modifier: Modifier = Modifier,
    currentScreen: Screen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    onHamburgClicked: () -> Unit = {},
    onCartClicked: () -> Unit = {},
    addButtonClick: () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(
                    0.dp,
                    0.dp,
                    12.dp,
                    12.dp
                )
            ),
        windowInsets = WindowInsets(top = 0.dp),
        title = {
            if (canNavigateBack) {
                currentScreen.title?.let {
                    AppTextSubTitle(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppTextSubTitle(
                        text = "Umar Awais",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        actions = {
            if (currentScreen == Screen.ForumScreen) {
                IconButton(onClick = addButtonClick) {
                    Icon(
                        imageVector = Icons.Sharp.Add,
                        contentDescription = null
                    )
                }
            }
            if (currentScreen == Screen.HomeScreen) {
                IconButton(onClick = onCartClicked) {
                    Icon(
                        imageVector = Icons.Sharp.Search,
                        contentDescription = null
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                        contentDescription = null
                    )
                }
            } else {
                IconButton(onClick = onHamburgClicked) {
                    Icon(
                        imageVector = Icons.Sharp.Person,
                        contentDescription = null
                    )
                }
            }
        }
    )
}