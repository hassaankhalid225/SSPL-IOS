package com.sspl.ui.faqs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme

data class FaqData(
    val id: Int,
    val question: String,
    val answer: String
)

private val OFFICIAL_FAQS = listOf(
    FaqData(
        id = 1,
        question = "Can MBBS students apply for membership?",
        answer = "No. It is mandatory to be a qualified surgeon before applying for membership. Students are encouraged to join our academic sessions but cannot hold official membership."
    ),
    FaqData(
        id = 2,
        question = "Is a specialization degree in Surgery mandatory for membership?",
        answer = "Yes. A recognized postgraduate specialization in surgery (such as FCPS, MS, or equivalent) is mandatory for membership in the Society of Surgeons of Pakistan."
    ),
    FaqData(
        id = 3,
        question = "Can allied healthcare workers apply for membership?",
        answer = "No. Membership of the Society of Surgeons of Pakistan is strictly limited to qualified surgeons. Allied healthcare professionals are currently not eligible for membership."
    ),
    FaqData(
        id = 4,
        question = "Can membership continue when migrating abroad?",
        answer = "Yes. Your SSP membership remains valid if you relocate abroad, provided your account is kept active and renewed according to SSP regulations."
    ),
    FaqData(
        id = 5,
        question = "Can government employees apply for membership?",
        answer = "Yes. Government-employed doctors and surgical specialists are fully eligible and encouraged to apply for SSP membership."
    ),
    FaqData(
        id = 6,
        question = "Can I apply if there is a chapter in my city?",
        answer = "Yes. You may apply for membership regardless of your location. Membership is open to all eligible surgeons across the country, irrespective of local chapter presence."
    ),
    FaqData(
        id = 7,
        question = "Can non-members register for society events?",
        answer = "Yes. Non-members are welcome to register for conferences and workshops. However, official members usually enjoy significantly discounted registration fees."
    ),
    FaqData(
        id = 8,
        question = "Can non-members present cases at society events?",
        answer = "Yes. Non-members may submit and present cases or research papers at SSP events, subject to abstract approval by the scientific committee."
    ),
    FaqData(
        id = 9,
        question = "Can I attend an event without registration?",
        answer = "No. Prior registration is mandatory for all SSP events to ensure proper organization, venue security, and the issuance of attendance certificates."
    )
)

@Composable
fun FaqsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    val filteredFaqs = OFFICIAL_FAQS.filter {
        it.question.contains(searchQuery, ignoreCase = true) ||
            it.answer.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "HELP & SUPPORT",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Frequently Asked Questions",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Find immediate answers regarding society membership eligibility, conferences, and procedures.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Search Field
        item {
            SsplTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search FAQs",
                placeholder = "Type keywords (e.g. membership, degree, events)...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // FAQ Items
        if (filteredFaqs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = AppTheme.colors.textMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No questions match your query",
                            style = AppTheme.typography.titleSmall,
                            color = AppTheme.colors.onSurface
                        )
                    }
                }
            }
        } else {
            items(filteredFaqs, key = { it.id }) { faq ->
                val isExpanded = expandedStates[faq.id] ?: false
                FaqAccordionCard(
                    faq = faq,
                    isExpanded = isExpanded,
                    onToggle = { expandedStates[faq.id] = !isExpanded }
                )
            }
        }
    }
}

@Composable
private fun FaqAccordionCard(
    faq: FaqData,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) AppTheme.colors.surface else AppTheme.colors.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isExpanded) AppTheme.colors.primary.copy(alpha = 0.4f) else AppTheme.colors.border
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isExpanded) AppTheme.colors.primary.copy(alpha = 0.12f) else AppTheme.colors.border.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Q",
                        style = AppTheme.typography.labelMedium,
                        color = if (isExpanded) AppTheme.colors.primary else AppTheme.colors.textMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = faq.question,
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isExpanded) AppTheme.colors.primary else AppTheme.colors.textMuted
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.background, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = faq.answer,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.88f),
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

