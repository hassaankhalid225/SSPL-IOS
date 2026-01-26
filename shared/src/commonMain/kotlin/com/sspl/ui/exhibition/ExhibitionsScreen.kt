package com.sspl.ui.exhibition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import coil3.compose.AsyncImage
import com.sspl.core.models.ExhibitionStall
import com.sspl.core.models.SponsorCategory
import com.sspl.theme.windowBackGround
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.utils.BASE_URL
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitionsScreen(
    navController: NavController,
    viewModel: ExhibitionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (uiState.isLoading && uiState.exhibitions.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.error != null && uiState.exhibitions.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTextSubTitle(text = uiState.error ?: "Error loading exhibitions", color = Color.Red)
                Button(onClick = { viewModel.loadExhibitions() }) {
                    Text("Retry")
                }
            }
        } else if (uiState.exhibitions.isEmpty()) {
            AppTextSubTitle(
                text = "No active exhibitions found.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.exhibitions) { exhibition ->
                    ExhibitionCard(exhibition = exhibition, navController = navController)
                }
            }
        }
    }
}

@Composable
fun ExhibitionCard(exhibition: ExhibitionStall, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Company Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!exhibition.companyLogo.isNullOrEmpty()) {
                        val fullImageUrl = if (exhibition.companyLogo.startsWith("/")) {
                            "$BASE_URL${exhibition.companyLogo}"
                        } else {
                            "$BASE_URL/${exhibition.companyLogo}"
                        }
                        AsyncImage(
                            model = fullImageUrl,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "No Logo",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        AppTextLabel(
                            text = exhibition.companyName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        CategoryBadge(category = exhibition.category)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AppTextSubTitle(
                        text = "Contact: ${exhibition.contactPerson}",
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AppTextSubTitle(
                            text = exhibition.contactNum,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    AppTextLabel(
                        text = "Products: ${exhibition.products.size}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val data = Json.encodeToString(exhibition)
                    navController.currentBackStackEntry?.savedStateHandle?.set("exhibition_data", data)
                    navController.navigate(Screen.ExhibitionDetailScreen.route)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "View Details",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(category: SponsorCategory) {
    val color = when (category) {
        SponsorCategory.GOLD -> Color(0xFFFFD700)
        SponsorCategory.SILVER -> Color(0xFFC0C0C0)
        SponsorCategory.BRONZE -> Color(0xFFCD7F32)
        SponsorCategory.PARTNER -> Color(0xFF2196F3)
        SponsorCategory.BASIC -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
