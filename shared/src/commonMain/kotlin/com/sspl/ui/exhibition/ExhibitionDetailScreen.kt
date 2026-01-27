package com.sspl.ui.exhibition

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.sspl.core.models.ExhibitionProduct
import com.sspl.core.models.ExhibitionStall
import com.sspl.theme.windowBackGround
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.AppTextTitle
import com.sspl.utils.BASE_URL
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExhibitionDetailScreen(
    navController: NavController,
) {
    val exhibitionData = navController.previousBackStackEntry?.savedStateHandle?.get<String>("exhibition_data")
    val viewModel: ExhibitionDetailViewModel = koinViewModel { parametersOf(exhibitionData) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppTextSubTitle(text = uiState.error!!, color = Color.Red)
            }
        } else {
            uiState.exhibition?.let { exhibition ->
                ExhibitionDetailContent(exhibition)
            }
        }
    }
}

@Composable
private fun ExhibitionDetailContent(exhibition: ExhibitionStall) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section with Logo and Name
        HeaderSection(exhibition)

        Column(modifier = Modifier.padding(16.dp)) {
            // Stats Section (Category, Stall No)
            StatsSection(exhibition)

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Information
            SectionTitle(title = "Contact Information", icon = Icons.Default.Info)
            Spacer(modifier = Modifier.height(12.dp))
            ContactInfoCard(exhibition)

            Spacer(modifier = Modifier.height(24.dp))

            // Products Section
            if (exhibition.products.isNotEmpty()) {
                SectionTitle(title = "Featured Products", icon = Icons.Default.ShoppingCart)
                Spacer(modifier = Modifier.height(12.dp))
                exhibition.products.forEach { product ->
                    ProductItem(product)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSection(exhibition: ExhibitionStall) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        windowBackGround
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White),
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
                            contentDescription = "Company Logo",
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AppTextLabel(
                text = exhibition.companyName,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun StatsSection(exhibition: ExhibitionStall) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBadge(
            label = "CATEGORY",
            value = exhibition.category.name,
            color = getCategoryColor(exhibition.category),
            modifier = Modifier.weight(1f)
        )
        StatBadge(
            label = "STALL NO",
            value = exhibition.stallNo ?: "TBD",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTextLabel(text = label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
            AppTextLabel(text = value, fontSize = 16.sp, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ContactInfoCard(exhibition: ExhibitionStall) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ContactRow(icon = Icons.Default.Person, label = "Contact Person", value = exhibition.contactPerson)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            ContactRow(icon = Icons.Default.Phone, label = "Phone Number", value = exhibition.contactNum)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            ContactRow(icon = Icons.Default.Email, label = "Email Address", value = exhibition.email)
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, size = 20.dp, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            AppTextLabel(text = label, fontSize = 12.sp, color = Color.Gray)
            AppTextLabel(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProductItem(product: ExhibitionProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppTextLabel(text = product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (product.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                AppTextSubTitle(text = product.description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        AppTextLabel(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Icon(imageVector: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}

private fun getCategoryColor(category: com.sspl.core.models.SponsorCategory): Color {
    return when (category) {
        com.sspl.core.models.SponsorCategory.GOLD -> Color(0xFFFFD700)
        com.sspl.core.models.SponsorCategory.SILVER -> Color(0xFFC0C0C0)
        com.sspl.core.models.SponsorCategory.BRONZE -> Color(0xFFCD7F32)
        com.sspl.core.models.SponsorCategory.PARTNER -> Color(0xFF2196F3)
        com.sspl.core.models.SponsorCategory.BASIC -> Color.Gray
    }
}
