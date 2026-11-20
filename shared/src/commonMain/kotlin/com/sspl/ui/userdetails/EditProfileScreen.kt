package com.sspl.ui.userdetails

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.ApiStates
import com.sspl.core.models.User
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PhoneWithCountrySelector
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

/**
 * A7: Modernized Edit Profile Screen.
 */
@Composable
fun EditProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = koinViewModel(),
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val isPersonalDetails =
        navController.previousBackStackEntry?.savedStateHandle?.get<Boolean>("isPersonalDetails") ?: true
    val user = navController.previousBackStackEntry?.savedStateHandle?.get<String>("currentUser")
    val userObj: User? = user?.let {
        try {
            Json.decodeFromString<User>(it)
        } catch (_: Exception) {
            null
        }
    }

    val basicInfoState by viewModel.basicProfileUiState.collectAsStateWithLifecycle()
    val professionalInfoState by viewModel.professionalProfileUiState.collectAsStateWithLifecycle()
    val scaffoldState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.updateProfessionalProfileUiState(userObj)
    }

    LaunchedEffect(Unit) {
        viewModel.isUserBasicInfoUpdated.collect { isBasicInfoUpdated ->
            when (isBasicInfoUpdated) {
                is ApiStates.Failure -> {
                    scaffoldState.showSnackbar(isBasicInfoUpdated.error.message)
                }
                is ApiStates.Success -> {
                    isBasicInfoUpdated.data?.let { profileViewModel.updateUser(it) }
                    navController.navigateUp()
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.isUserProfessionalInfoUpdated.collect { isProfessionalInfoUpdated ->
            when (isProfessionalInfoUpdated) {
                is ApiStates.Failure -> {
                    scaffoldState.showSnackbar(isProfessionalInfoUpdated.error.message)
                }
                is ApiStates.Success -> {
                    isProfessionalInfoUpdated.data?.let { profileViewModel.updateUserDetails(it) }
                    navController.navigateUp()
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        containerColor = AppTheme.colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .imePadding()
            ) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.onPrimary
                        )
                    }
                    Text(
                        text = if (isPersonalDetails) "Edit Personal Details" else "Edit Professional Profile",
                        style = AppTheme.typography.titleMedium.copy(
                            color = AppTheme.colors.heading,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    if (isPersonalDetails) {
                        EditPersonalDetails(
                            basicProfileUiState = basicInfoState,
                            onEvent = viewModel::onBasicProfileEvent
                        )
                    } else {
                        EditProfessionalDetails(
                            state = professionalInfoState,
                            onEvent = viewModel::onProfessionalEvent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EditPersonalDetails(
    basicProfileUiState: BasicProfileUiState,
    onEvent: (BasicProfileEvents) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SsplTextField(
            value = basicProfileUiState.firstName,
            onValueChange = { onEvent(BasicProfileEvents.UpdateFirstName(it)) },
            label = "First Name",
            placeholder = "First name",
            leadingIcon = Icons.Default.Person,
            errorMessage = basicProfileUiState.firstNameError,
            isError = basicProfileUiState.firstNameError != null
        )

        SsplTextField(
            value = basicProfileUiState.lastName,
            onValueChange = { onEvent(BasicProfileEvents.UpdateLastName(it)) },
            label = "Last Name",
            placeholder = "Last name",
            errorMessage = basicProfileUiState.lastNameError,
            isError = basicProfileUiState.lastNameError != null
        )

        PhoneWithCountrySelector(
            selectedCountry = basicProfileUiState.country,
            showTransformation = false,
            phone = basicProfileUiState.phone,
            errorMessage = basicProfileUiState.phoneNoError,
            onPhoneNumberChange = { onEvent(BasicProfileEvents.UpdatePhone(it)) },
            showCountryChooser = basicProfileUiState.showCountryChooser,
            onCountryChange = { onEvent(BasicProfileEvents.OnCountryChange(it)) },
            toggleCountryPicker = { onEvent(BasicProfileEvents.ToggleCountryChooser) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PrimaryButton(
            text = "Save Changes",
            onClick = { onEvent(BasicProfileEvents.OnSubmit) },
            isLoading = basicProfileUiState.isLoading,
            enabled = basicProfileUiState.isUpdateEnabled && !basicProfileUiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EditProfessionalDetails(
    state: ProfessionalProfileUiState,
    onEvent: (ProfessionalProfileEvents) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SsplTextField(
            value = state.pmDcNumber.orEmpty(),
            onValueChange = { onEvent(ProfessionalProfileEvents.OnPmDcNumberTextChange(it)) },
            label = "PMDC / Council Number",
            placeholder = "e.g. 12345-P",
            leadingIcon = Icons.Default.MedicalServices,
            errorMessage = state.pmDcNumberError,
            isError = state.pmDcNumberError != null
        )

        SsplTextField(
            value = state.designation,
            onValueChange = { onEvent(ProfessionalProfileEvents.OnDesignationTextChange(it)) },
            label = "Designation / Title",
            placeholder = "e.g. Consultant Surgeon",
            leadingIcon = Icons.Default.Work,
            errorMessage = state.designationError,
            isError = state.designationError != null
        )

        SsplTextField(
            value = state.institution,
            onValueChange = { onEvent(ProfessionalProfileEvents.OnInstitutionTextChange(it)) },
            label = "Institution / Hospital",
            placeholder = "e.g. Services Hospital Lahore",
            leadingIcon = Icons.Default.Business,
            errorMessage = state.institutionError,
            isError = state.institutionError != null
        )

        SsplTextField(
            value = state.registrationNumber ?: "",
            onValueChange = { onEvent(ProfessionalProfileEvents.OnRegistrationNumberTextChange(it)) },
            label = "Society Registration Number",
            placeholder = "Optional registration number",
            errorMessage = state.registrationNumberError,
            isError = state.registrationNumberError != null
        )

        Spacer(modifier = Modifier.height(10.dp))

        PrimaryButton(
            text = "Save Changes",
            onClick = { onEvent(ProfessionalProfileEvents.OnSave) },
            isLoading = state.isLoading,
            enabled = state.isUpdateEnabled && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
