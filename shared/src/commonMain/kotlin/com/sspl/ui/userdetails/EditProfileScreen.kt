package com.sspl.ui.userdetails

import com.sspl.designsystem.icons.SsplIcons

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.ApiStates
import com.sspl.core.models.User
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PhoneWithCountrySelector
import com.sspl.ui.kyc.FormGroupLabel
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

/**
 * Edit profile (inside the main shell). Opens in "personal" or "professional" mode depending on the
 * flag the profile screen put in the saved-state handle. Save lives in a sticky bottom bar.
 */
@Composable
fun EditProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = koinViewModel(),
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val prevHandle = navController.previousBackStackEntry?.savedStateHandle
    val isPersonalDetails = remember { prevHandle?.get<Boolean>("isPersonalDetails") ?: true }
    val userObj: User? = remember {
        prevHandle?.get<String>("currentUser")?.let {
            runCatching { Json { ignoreUnknownKeys = true }.decodeFromString(User.serializer(), it) }.getOrNull()
        }
    }

    val basicInfoState by viewModel.basicProfileUiState.collectAsStateWithLifecycle()
    val professionalInfoState by viewModel.professionalProfileUiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { viewModel.updateProfessionalProfileUiState(userObj) }

    LaunchedEffect(Unit) {
        viewModel.isUserBasicInfoUpdated.collect { result ->
            when (result) {
                is ApiStates.Failure -> snackbar.showSnackbar(
                    result.error.message.ifBlank { "Couldn't save your changes. Please try again." }
                )
                is ApiStates.Success -> {
                    result.data?.let { profileViewModel.updateUser(it) }
                    navController.navigateUp()
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.isUserProfessionalInfoUpdated.collect { result ->
            when (result) {
                is ApiStates.Failure -> snackbar.showSnackbar(
                    result.error.message.ifBlank { "Couldn't save your changes. Please try again." }
                )
                is ApiStates.Success -> {
                    result.data?.let { profileViewModel.updateUserDetails(it) }
                    navController.navigateUp()
                }
                else -> Unit
            }
        }
    }

    val loading = if (isPersonalDetails) basicInfoState.isLoading else professionalInfoState.isLoading
    val enabled = if (isPersonalDetails) basicInfoState.isUpdateEnabled else professionalInfoState.isUpdateEnabled
    val save: () -> Unit = {
        focusManager.clearFocus()
        if (isPersonalDetails) viewModel.onBasicProfileEvent(BasicProfileEvents.OnSubmit)
        else viewModel.onProfessionalEvent(ProfessionalProfileEvents.OnSave)
    }

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EditIntro(
                    icon = if (isPersonalDetails) SsplIcons.Person else SsplIcons.MedicalServices,
                    title = if (isPersonalDetails) "Personal info" else "Professional details",
                    caption = if (isPersonalDetails) "Your name and mobile number as shown to SSPL."
                    else "Credentials used to verify your membership."
                )
                if (isPersonalDetails) {
                    EditPersonalDetails(basicProfileUiState = basicInfoState, onEvent = viewModel::onBasicProfileEvent)
                } else {
                    EditProfessionalDetails(
                        state = professionalInfoState,
                        onEvent = viewModel::onProfessionalEvent,
                        onDone = save
                    )
                }
            }
            StickyCtaBar(
                primaryText = "Save changes",
                onPrimaryClick = save,
                loading = loading,
                enabled = enabled && !loading,
                secondaryText = "Cancel",
                onSecondaryClick = { navController.navigateUp() }
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp, start = 12.dp, end = 12.dp))
    }
}

@Composable
private fun EditIntro(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, caption: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.titleMedium)
            Text(caption, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EditPersonalDetails(
    basicProfileUiState: BasicProfileUiState,
    onEvent: (BasicProfileEvents) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) },
        onDone = { focusManager.moveFocus(FocusDirection.Down) }
    )
    val busy = basicProfileUiState.isLoading

    SsplCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FormGroupLabel("Name")
            SsplTextField(
                value = basicProfileUiState.firstName,
                onValueChange = { onEvent(BasicProfileEvents.UpdateFirstName(it)) },
                label = "First name",
                placeholder = "First name",
                leadingIcon = SsplIcons.Person,
                errorMessage = basicProfileUiState.firstNameError,
                enabled = !busy,
                keyboardActions = next
            )
            SsplTextField(
                value = basicProfileUiState.lastName,
                onValueChange = { onEvent(BasicProfileEvents.UpdateLastName(it)) },
                label = "Last name",
                placeholder = "Last name",
                errorMessage = basicProfileUiState.lastNameError,
                enabled = !busy,
                keyboardActions = next
            )
        }
    }
    SsplCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FormGroupLabel("Contact")
            Text("Mobile number", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
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
            Text(
                "Your email address can't be changed here.",
                style = AppTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun EditProfessionalDetails(
    state: ProfessionalProfileUiState,
    onEvent: (ProfessionalProfileEvents) -> Unit,
    onDone: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) },
        onDone = { focusManager.moveFocus(FocusDirection.Down) }
    )
    val busy = state.isLoading

    SsplCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FormGroupLabel("Credentials")
            SsplTextField(
                value = state.pmDcNumber.orEmpty(),
                onValueChange = { onEvent(ProfessionalProfileEvents.OnPmDcNumberTextChange(it)) },
                label = "PMDC / council number",
                placeholder = "e.g. 12345-P",
                leadingIcon = SsplIcons.MedicalServices,
                errorMessage = state.pmDcNumberError,
                enabled = !busy,
                keyboardActions = next
            )
            SsplTextField(
                value = state.registrationNumber,
                onValueChange = { onEvent(ProfessionalProfileEvents.OnRegistrationNumberTextChange(it)) },
                label = "Society registration number",
                placeholder = "Registration number",
                leadingIcon = SsplIcons.Badge,
                errorMessage = state.registrationNumberError,
                keyboardType = KeyboardType.Text,
                enabled = !busy,
                keyboardActions = next
            )
        }
    }
    SsplCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FormGroupLabel("Workplace")
            SsplTextField(
                value = state.designation,
                onValueChange = { onEvent(ProfessionalProfileEvents.OnDesignationTextChange(it)) },
                label = "Designation",
                placeholder = "e.g. Consultant Surgeon",
                leadingIcon = SsplIcons.Work,
                errorMessage = state.designationError,
                enabled = !busy,
                keyboardActions = next
            )
            SsplTextField(
                value = state.institution,
                onValueChange = { onEvent(ProfessionalProfileEvents.OnInstitutionTextChange(it)) },
                label = "Institution / hospital",
                placeholder = "e.g. Services Hospital, Lahore",
                leadingIcon = SsplIcons.Business,
                errorMessage = state.institutionError,
                enabled = !busy,
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (state.isUpdateEnabled) onDone()
                })
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}
