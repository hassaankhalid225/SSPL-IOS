package com.sspl.ui.userdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import com.sspl.ui.components.CountrySelectorField
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A3 (Step 2): Professional KYC profile setup.
 */
@Composable
fun UserDetailsScreen(
    navController: NavController,
    viewModel: UserDetailViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.networkState.collect { event ->
            when (event) {
                is ApiStates.Failure -> {
                    scaffoldState.showSnackbar(event.error.message)
                }
                is ApiStates.Success -> {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.UserDetailsScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        containerColor = AppTheme.colors.background
    ) { padding ->
        UserDetailsScreenContent(
            modifier = modifier.padding(padding),
            pmdcNumber = uiState.pmDcNumber ?: "",
            pmdcNumberError = uiState.pmDcNumberError,
            isSaveEnabled = uiState.isSaveEnabled,
            isLoading = uiState.isLoading,
            city = uiState.city,
            cityError = uiState.cityError,
            showCountryPicker = uiState.showCountryPicker,
            country = uiState.country,
            countryError = uiState.countryError,
            institution = uiState.institution,
            registrationError = uiState.registrationNumberError,
            institutionError = uiState.institutionError,
            designation = uiState.designation,
            designationError = uiState.designationError,
            registrationNumber = uiState.registrationNumber,
            onSkip = {
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.UserDetailsScreen.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
fun UserDetailsScreenContent(
    modifier: Modifier = Modifier,
    pmdcNumber: String,
    pmdcNumberError: String?,
    isSaveEnabled: Boolean,
    registrationError: String?,
    isLoading: Boolean,
    city: String,
    cityError: String?,
    country: String,
    countryError: String?,
    showCountryPicker: Boolean,
    institution: String,
    institutionError: String?,
    designation: String,
    designationError: String?,
    registrationNumber: String?,
    onSkip: () -> Unit,
    onEvent: (UserDetailsUiEvents) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Logo & Header
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = "SSPL Logo",
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Professional Profile",
                style = AppTheme.typography.headlineMedium.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Provide your medical credentials for membership verification",
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.muted
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator (Step 2 of 2)
            StepIndicator(
                currentStep = 2,
                totalSteps = 2,
                stepLabels = listOf("Account Info", "Professional KYC")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fields Card
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SsplTextField(
                        value = pmdcNumber,
                        onValueChange = { onEvent(UserDetailsUiEvents.OnPmDcNumberTextChange(it)) },
                        label = "PMDC / Medical Council Number",
                        placeholder = "e.g. 12345-P",
                        leadingIcon = Icons.Default.MedicalServices,
                        errorMessage = pmdcNumberError,
                        isError = pmdcNumberError != null
                    )

                    SsplTextField(
                        value = designation,
                        onValueChange = { onEvent(UserDetailsUiEvents.OnDesignationTextChange(it)) },
                        label = "Designation / Title",
                        placeholder = "e.g. Consultant Plastic Surgeon",
                        leadingIcon = Icons.Default.Work,
                        errorMessage = designationError,
                        isError = designationError != null
                    )

                    SsplTextField(
                        value = institution,
                        onValueChange = { onEvent(UserDetailsUiEvents.OnInstitutionTextChange(it)) },
                        label = "Hospital / Institution",
                        placeholder = "e.g. Civil Hospital / Dow University",
                        leadingIcon = Icons.Default.Business,
                        errorMessage = institutionError,
                        isError = institutionError != null
                    )

                    SsplTextField(
                        value = city,
                        onValueChange = { onEvent(UserDetailsUiEvents.OnCityTextChange(it)) },
                        label = "City",
                        placeholder = "e.g. Karachi",
                        leadingIcon = Icons.Default.LocationCity,
                        errorMessage = cityError,
                        isError = cityError != null
                    )

                    CountrySelectorField(
                        country = country,
                        onCountryChange = { onEvent(UserDetailsUiEvents.OnCountryChange(it?.name ?: "")) },
                        errorMessage = countryError,
                        toggleCountryPicker = { onEvent(UserDetailsUiEvents.ToggleCountryPicker) },
                        showCountryChooser = showCountryPicker
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PrimaryButton(
                        text = "Complete Profile",
                        onClick = { onEvent(UserDetailsUiEvents.OnSave) },
                        isLoading = isLoading,
                        enabled = isSaveEnabled && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip for now
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButtonLink(
                    text = "Skip for now, I'll complete it later",
                    onClick = onSkip
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
