package com.sspl.ui.userdetails

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.CountrySelectorField
import com.sspl.ui.kyc.AuthScaffold
import com.sspl.ui.kyc.FormGroupLabel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sign up — step 2 of 2: professional details (optional; can be skipped).
 * Shown right after account creation, outside the main shell.
 */
@Composable
fun UserDetailsScreen(
    navController: NavController,
    viewModel: UserDetailViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val goToApp: () -> Unit = {
        navController.navigate(Screen.MainScreen.route) {
            popUpTo(Screen.UserDetailsScreen.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.networkState.collect { event ->
            when (event) {
                is ApiStates.Failure -> snackbar.showSnackbar(
                    event.error.message.ifBlank { "Couldn't save your details. Please try again." }
                )
                is ApiStates.Success -> goToApp()
                else -> Unit
            }
        }
    }

    AuthScaffold(
        title = "Professional details",
        subtitle = "Help us verify your membership. You can finish this later.",
        snackbarHostState = snackbar,
        heroFraction = 0.32f,
        modifier = modifier
    ) {
        UserDetailsScreenContent(
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
            onSkip = goToApp,
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
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) },
        onDone = { focusManager.moveFocus(FocusDirection.Down) }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        StepIndicator(
            currentStep = 2,
            totalSteps = 2,
            stepLabels = listOf("Account", "Professional details")
        )
        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FormGroupLabel("Credentials")
            SsplTextField(
                value = pmdcNumber,
                onValueChange = { onEvent(UserDetailsUiEvents.OnPmDcNumberTextChange(it)) },
                label = "PMDC / medical council number (optional)",
                placeholder = "e.g. 12345-P",
                leadingIcon = SsplIcons.MedicalServices,
                errorMessage = pmdcNumberError,
                enabled = !isLoading,
                keyboardActions = next
            )
            SsplTextField(
                value = designation,
                onValueChange = { onEvent(UserDetailsUiEvents.OnDesignationTextChange(it)) },
                label = "Designation (optional)",
                placeholder = "e.g. Consultant Surgeon",
                leadingIcon = SsplIcons.Work,
                errorMessage = designationError,
                enabled = !isLoading,
                keyboardActions = next
            )

            FormGroupLabel("Workplace")
            SsplTextField(
                value = institution,
                onValueChange = { onEvent(UserDetailsUiEvents.OnInstitutionTextChange(it)) },
                label = "Institute (optional)",
                placeholder = "Organization name",
                leadingIcon = SsplIcons.Business,
                errorMessage = institutionError,
                enabled = !isLoading,
                keyboardActions = next
            )
            SsplTextField(
                value = city,
                onValueChange = { onEvent(UserDetailsUiEvents.OnCityTextChange(it)) },
                label = "City (optional)",
                placeholder = "Lahore",
                leadingIcon = SsplIcons.LocationOn,
                errorMessage = cityError,
                enabled = !isLoading,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }, onNext = { focusManager.clearFocus() })
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Country (optional)", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
                CountrySelectorField(
                    country = country,
                    onCountryChange = { onEvent(UserDetailsUiEvents.OnCountryChange(it?.name ?: "")) },
                    errorMessage = countryError,
                    toggleCountryPicker = { onEvent(UserDetailsUiEvents.ToggleCountryPicker) },
                    showCountryChooser = showCountryPicker
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        PrimaryButton(
            text = "Complete profile",
            onClick = {
                focusManager.clearFocus()
                onEvent(UserDetailsUiEvents.OnSave)
            },
            loading = isLoading,
            enabled = isSaveEnabled,
            icon = SsplIcons.CheckCircle
        )
        Spacer(Modifier.height(6.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TextButtonLink(text = "Skip for now", onClick = onSkip, enabled = !isLoading)
        }
    }
}
