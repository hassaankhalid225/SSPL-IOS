package com.sspl.ui.fellowship

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.models.FellowApplicationRequest
import com.sspl.core.models.TrainingCenter
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * E4: Apply as Fellow Form Screen
 */
@Composable
fun FellowApplicationScreen(
    navController: NavController,
    fellowshipRepository: FellowshipRepository = koinInject(),
    userSession: UserSession = koinInject()
) {
    val user by userSession.userFlow.collectAsState()
    val scope = rememberCoroutineScope()

    var name by remember {
        mutableStateOf(listOfNotNull(user?.prefix, user?.firstName, user?.lastName).joinToString(" ").ifBlank { "" })
    }
    var email by remember { mutableStateOf(user?.account?.email ?: user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var pmdc by remember { mutableStateOf(user?.pmdc ?: "") }
    var qualifications by remember { mutableStateOf(user?.institution ?: "FCPS Surgery") }
    var currentInstitution by remember { mutableStateOf(user?.institution ?: "") }
    var statement by remember { mutableStateOf("") }

    var centers by remember { mutableStateOf<List<TrainingCenter>>(emptyList()) }
    var selectedCenter by remember { mutableStateOf<TrainingCenter?>(null) }
    var isCenterDropdownOpen by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fellowshipRepository.getTrainingCenters().collect { list ->
            centers = list
            selectedCenter = list.firstOrNull()
        }
    }

    val handleSubmit = {
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            statusMessage = "Please complete all mandatory personal fields."
            isError = true
        } else if (pmdc.isBlank()) {
            statusMessage = "PMDC registration number is required."
            isError = true
        } else {
            scope.launch {
                isSubmitting = true
                statusMessage = null
                val result = fellowshipRepository.submitFellowApplication(
                    FellowApplicationRequest(
                        programId = 1L,
                        applicantName = name,
                        email = email,
                        phone = phone,
                        pmdc = pmdc,
                        qualifications = qualifications,
                        currentInstitution = currentInstitution,
                        preferredCenterId = selectedCenter?.id,
                        statement = statement
                    )
                )
                isSubmitting = false
                if (result.isSuccess) {
                    statusMessage = "Fellowship application submitted successfully! Our academic committee will reach out after credentials review."
                    isError = false
                } else {
                    statusMessage = result.exceptionOrNull()?.message ?: "Application submission failed. Please try again."
                    isError = true
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CANDIDATE ADMISSION",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Apply as a Fellow",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Candidate admission form for the next annual academic fellowship cycle.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        if (statusMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isError) AppTheme.colors.error.copy(alpha = 0.1f)
                            else AppTheme.colors.accent.copy(alpha = 0.15f)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = statusMessage!!,
                        style = AppTheme.typography.bodySmall.copy(
                            color = if (isError) AppTheme.colors.error else AppTheme.colors.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Candidate Credentials",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SsplTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name *",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address *",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Mobile Phone *",
                        placeholder = "03001234567",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = pmdc,
                        onValueChange = { pmdc = it },
                        label = "PMDC / PMC Registration Number *",
                        placeholder = "e.g. 12345-P",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = qualifications,
                        onValueChange = { qualifications = it },
                        label = "Postgraduate Qualifications",
                        placeholder = "e.g. FCPS Surgery, FRCS, MRCS",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = currentInstitution,
                        onValueChange = { currentInstitution = it },
                        label = "Current Hospital / Affiliation",
                        placeholder = "e.g. Services Hospital Lahore",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Training Center Choice
                    Text(
                        text = "Preferred Training Centre",
                        style = AppTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.heading
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.surface)
                            .clickable { isCenterDropdownOpen = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCenter?.name ?: "Select Preferred Centre",
                                style = AppTheme.typography.bodyMedium.copy(
                                    color = if (selectedCenter != null) AppTheme.colors.heading else AppTheme.colors.muted
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AppTheme.colors.muted
                            )
                        }

                        DropdownMenu(
                            expanded = isCenterDropdownOpen,
                            onDismissRequest = { isCenterDropdownOpen = false }
                        ) {
                            centers.forEach { center ->
                                DropdownMenuItem(
                                    text = { Text("${center.name} (${center.city ?: ""})") },
                                    onClick = {
                                        selectedCenter = center
                                        isCenterDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SsplTextField(
                        value = statement,
                        onValueChange = { statement = it },
                        label = "Statement of Purpose (Clinical interest & goals)",
                        placeholder = "Explain your subspecialty aspirations...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SsplButton(
                        text = if (isSubmitting) "Submitting Application..." else "Submit Application",
                        onClick = { handleSubmit() },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

