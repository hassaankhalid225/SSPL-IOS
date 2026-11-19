package com.sspl.ui.fellowship

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.models.TrainingCenterApplicationRequest
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * E5: Apply as Training Centre Application Screen
 */
@Composable
fun CenterApplicationScreen(
    navController: NavController,
    fellowshipRepository: FellowshipRepository = koinInject()
) {
    val scope = rememberCoroutineScope()

    var centerName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var headOfDepartment by remember { mutableStateOf("") }
    var supervisorName by remember { mutableStateOf("") }
    var supervisorEmail by remember { mutableStateOf("") }
    var supervisorPhone by remember { mutableStateOf("") }
    var bedCapacity by remember { mutableStateOf("25") }
    var monthlyOpd by remember { mutableStateOf("300") }
    var annualSurgeries by remember { mutableStateOf("500") }
    var facilities by remember { mutableStateOf("Dedicated Plastic Surgery OT, Burn ICU, Microsurgery microscope") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val handleSubmit = {
        if (centerName.isBlank() || city.isBlank() || headOfDepartment.isBlank() || supervisorName.isBlank()) {
            statusMessage = "Please complete all mandatory institution fields."
            isError = true
        } else {
            scope.launch {
                isSubmitting = true
                statusMessage = null
                val result = fellowshipRepository.submitCenterApplication(
                    TrainingCenterApplicationRequest(
                        centerName = centerName,
                        city = city,
                        address = address,
                        headOfDepartment = headOfDepartment,
                        supervisorName = supervisorName,
                        supervisorEmail = supervisorEmail,
                        supervisorPhone = supervisorPhone,
                        bedCapacity = bedCapacity.toIntOrNull(),
                        monthlyOpdVolume = monthlyOpd.toIntOrNull(),
                        annualSurgeries = annualSurgeries.toIntOrNull(),
                        facilities = facilities
                    )
                )
                isSubmitting = false
                if (result.isSuccess) {
                    statusMessage = "Training Centre application submitted! The SSPL accreditation inspectorate will schedule an on-site evaluation."
                    isError = false
                } else {
                    statusMessage = result.exceptionOrNull()?.message ?: "Application failed. Please try again."
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
                text = "INSTITUTIONAL ACCREDITATION",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Apply as Training Centre",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Institutional accreditation application for tertiary hospital departments & specialized burn centers.",
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
                        text = "Hospital / Unit Details",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SsplTextField(
                        value = centerName,
                        onValueChange = { centerName = it },
                        label = "Hospital & Department Name *",
                        placeholder = "e.g. Dept of Plastic Surgery, Dow University Hospital",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City *",
                        placeholder = "e.g. Karachi",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Full Physical Address",
                        placeholder = "Campus / street address",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = headOfDepartment,
                        onValueChange = { headOfDepartment = it },
                        label = "Head of Department (Professor / Chief) *",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = supervisorName,
                        onValueChange = { supervisorName = it },
                        label = "Designated Fellowship Supervisor *",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = supervisorEmail,
                        onValueChange = { supervisorEmail = it },
                        label = "Supervisor Email",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = supervisorPhone,
                        onValueChange = { supervisorPhone = it },
                        label = "Supervisor Phone",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SsplTextField(
                        value = facilities,
                        onValueChange = { facilities = it },
                        label = "Clinical Facilities & Specialized Equipment",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SsplButton(
                        text = if (isSubmitting) "Submitting Application..." else "Submit Accreditation Application",
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

