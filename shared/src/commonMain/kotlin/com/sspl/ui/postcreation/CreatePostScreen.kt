package com.sspl.ui.postcreation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.resources.Res
import com.sspl.resources.ad_sspl
import com.sspl.resources.camera
import com.sspl.resources.camera_ic
import com.sspl.resources.gallery
import com.sspl.resources.gallery_ic
import com.sspl.resources.post
import com.sspl.resources.whats_on_your_mind
import com.sspl.theme.primary
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.AppTextTitle
import com.sspl.ui.components.BetterModalBottomSheet
import com.sspl.ui.components.ButtonWithProgress
import com.sspl.ui.components.RoundedImageView
import com.sspl.ui.components.countrypicker.Country
import com.sspl.ui.components.countrypicker.CountryList
import com.sspl.ui.components.countrypicker.SearchBar
import com.sspl.ui.components.countrypicker.allCountries
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: PostCreationViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    CreatePostContent(
        descriptionText = uiState.description,
        isLoginEnabled = uiState.isUploadEnabled,
        addImage = {
            viewModel.onEvent(CreatePostUIEvents.ToggleBottomSheet(true))
        },
        isLoading = uiState.isLoading,
        images = uiState.images,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
    AnimatedVisibility (uiState.showChooserBottomSheet) {
        ChooserBottomSheet(onCameraSelected = {
            viewModel.onEvent(CreatePostUIEvents.ToggleBottomSheet(false))

//
        }, onGallerySelected = {
            viewModel.onEvent(CreatePostUIEvents.ToggleBottomSheet(false))

//
        }, onDismiss = {
            viewModel.onEvent(CreatePostUIEvents.ToggleBottomSheet(false))
        })
    }
}

@Composable
fun CreatePostContent(
    modifier: Modifier = Modifier,
    isLoginEnabled: Boolean,
    images: List<String>,
    isLoading: Boolean,
    addImage: (Int) -> Unit,
    descriptionText: String,
    onEvent: (CreatePostUIEvents) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier.fillMaxSize().background(Color.White).padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    keyboardController?.hide()
                }
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {

        DescriptionTextEditor(text = descriptionText,
            onTextChanged = { onEvent(CreatePostUIEvents.OnDescriptionChange(it)) }

        )
        Spacer(Modifier.height(20.dp))
        AddImages(images = images, onAddImageClick = addImage, onDeleteImageClick = {
            onEvent.invoke(CreatePostUIEvents.OnImageDelete(it))
        })

        Spacer(Modifier.weight(0.15f))
        ButtonWithProgress(
            onClick = {
                onEvent(CreatePostUIEvents.UploadPost)
            },
            isEnabled = isLoginEnabled,
            isLoading = isLoading,
            buttonText = stringResource(Res.string.post),
            modifier = Modifier.imePadding()
        )
    }
}

@Composable
fun DescriptionTextEditor(
    modifier: Modifier = Modifier, text: String, onTextChanged: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.7.dp, Color.LightGray),
        modifier = Modifier.imePadding()
    ) {
        TextField(
            value = text,
            onValueChange = {
                onTextChanged(it)
            },
            modifier = modifier.fillMaxWidth().heightIn(min = 160.dp, max = 300.dp).padding(10.dp),
            placeholder = {
                AppTextLabel(
                    stringResource(Res.string.whats_on_your_mind), color = Color.Gray
                )
            },
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black, textDecoration = null),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AddImages(
    images: List<String>, onAddImageClick: (Int) -> Unit, onDeleteImageClick: (Int) -> Unit
) {
    val imageCount = images.size
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.7.dp, Color.LightGray),
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            when (imageCount) {
                0 -> ImageAddState(onAddImageClick, imageCount = 0)
                1 -> OneImageWithAddState(images.first(), onAddImageClick, onDeleteImageClick)
                2 -> TwoImagesState(images, onDeleteImageClick)
                else -> ImageAddState(onAddImageClick, imageCount = 2)
            }
        }
    }
}

@Composable
fun ImageAddState(onAddImageClick: (Int) -> Unit, imageCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize().clickable { onAddImageClick(imageCount) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (imageCount) {
                1 -> "Add second image"
                2 -> "Add images (Max 2)"
                else -> "Select an image" // Default message for zero images
            }, textAlign = TextAlign.Center
        )
        Text(
            text = "Upload from Gallery or take with Camera",
            style = TextStyle(fontSize = 14.sp, color = Color.Gray),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun OneImageWithAddState(
    image: String, onAddImageClick: (Int) -> Unit, onDeleteImageClick: (Int) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        // Display the first image with delete option
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 4.dp)
        ) {
            RoundedImageView(
                image = image, modifier = Modifier.fillMaxSize()
            )
            // Add delete button on top of the image
            DeleteImageButton(modifier = Modifier.align(Alignment.TopEnd),
                onDeleteImageClick = { onDeleteImageClick(0) })
        }

        // Show the "Add second image" option
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().clickable { onAddImageClick(2) },
            contentAlignment = Alignment.Center
        ) {
            ImageAddState(onAddImageClick, imageCount = 1)
        }
    }
}

@Composable
fun TwoImagesState(images: List<String>, onDeleteImageClick: (Int) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        images.take(2).forEachIndexed { index, image ->
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
            ) {
                RoundedImageView(image = image, modifier = Modifier.fillMaxSize())
                // Add delete button on top of each image
                DeleteImageButton(modifier = Modifier.align(Alignment.TopEnd),
                    onDeleteImageClick = { onDeleteImageClick(index) })
            }
        }
    }
}

@Composable
fun DeleteImageButton(modifier: Modifier = Modifier, onDeleteImageClick: () -> Unit) {
    Icon(imageVector = Icons.Outlined.Delete,
        contentDescription = "Delete",
        tint = primary,
        modifier = modifier.padding(6.dp).size(30.dp)
            .clickable { onDeleteImageClick() } // Make the Icon clickable
            .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
            .padding(5.dp) // Optional background for better visibility

    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooserBottomSheet(
    modifier: Modifier = Modifier,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val scope = rememberCoroutineScope()

    BetterModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier
    ) {

        Row(modifier.padding(16.dp)) {
            // Gallery Column
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        scope.launch { sheetState.hide() }
                        onGallerySelected()
                    }, verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.gallery_ic),
                    contentDescription = stringResource(Res.string.gallery),
                    modifier = Modifier.size(48.dp) // Adjust size if necessary
                )
                AppTextSubTitle(stringResource(Res.string.gallery))
            }

            Spacer(Modifier.width(16.dp))

            // Camera Column
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        scope.launch { sheetState.hide() }
                        onCameraSelected()

        }, verticalArrangement = Arrangement.spacedBy(6.dp)

            ) {
                Image(
                    painter = painterResource(Res.drawable.camera_ic),
                    contentDescription = stringResource(Res.string.camera),
                    modifier = Modifier.size(48.dp) // Adjust size if necessary
                )
                AppTextSubTitle(stringResource(Res.string.camera))
            }
        }
    }
}




