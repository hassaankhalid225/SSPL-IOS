package com.sspl.ui.userdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.resources.Res
import com.sspl.resources.city
import com.sspl.resources.country
import com.sspl.resources.designation
import com.sspl.resources.enter_pmdc_number
import com.sspl.resources.enter_registration_number
import com.sspl.resources.fill_out_the_details_below_to_finish_setting_up_your_account
import com.sspl.resources.institution_name
import com.sspl.resources.login_screen_background
import com.sspl.resources.personal_details
import com.sspl.resources.save_details
import com.sspl.resources.skip_for_now
import com.sspl.ui.components.AppTextField
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.ButtonWithProgress
import com.sspl.ui.kyc.HeaderSection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserDetailsScreen(
    navController: NavController,
    viewModel: UserDetailViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UserDetailsScreenContent(modifier= modifier,
        pmdcNumber = uiState.pmDcNumber,
        pmdcNumberError = uiState.pmDcNumberError,
        isSaveEnabled = uiState.isSaveEnabled,
        isLoading = uiState.isLoading,
        city = uiState.city,
        cityError = uiState.cityError,
        country = uiState.country,
        countryError = uiState.countryError,
        institution = uiState.institution,
        institutionError = uiState.institutionError,
        designation = uiState.designation,
        designationError = uiState.designationError,
        registrationNumber = uiState.registrationNumber,onSkip={
            navController.navigate(Screen.HomeScreen.route)
        },
        onEvent = viewModel::onEvent
        )
}

@Composable
fun UserDetailsScreenContent(
    modifier: Modifier = Modifier,
    pmdcNumber: String,
    pmdcNumberError: String?,
    isSaveEnabled: Boolean,
    isLoading: Boolean,
    city: String,
    cityError: String?,
    country: String,
    countryError: String?,
    institution: String,
    institutionError: String?,
    designation: String,
    designationError: String?,
    registrationNumber: String?,onSkip:()->Unit,
    onEvent: (UserDetailsUiEvents) -> Unit
) {
    Box(modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(Res.drawable.login_screen_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        // Main Column
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Spacer(Modifier.weight(0.15f))
            HeaderSection(
                title = stringResource(Res.string.personal_details),
                subTitle = stringResource(Res.string.fill_out_the_details_below_to_finish_setting_up_your_account)
            )
            Spacer(Modifier.weight(0.15f))

            // LoginFields Section (Handles all input fields)
            UserDetailFields(
                pmdcNumber = pmdcNumber,
                pmdcNumberError = pmdcNumberError,
                institution = institution,
                institutionError = institutionError,
                country = country,
                countryError = countryError,
                city = city,
                cityError = cityError,
                designation = designation,
                designationError = designationError,
                registrationNumber = registrationNumber,
                onEvent = onEvent
            )

            // Save Button
            Spacer(modifier = Modifier.height(10.dp))
            ButtonWithProgress(
                isEnabled = isSaveEnabled,
                isLoading = isLoading,
                onClick = { onEvent(UserDetailsUiEvents.OnSave) },
                buttonText = stringResource(Res.string.save_details)
            )
            AppTextLabel(text = stringResource(Res.string.skip_for_now),
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onSkip() })

            Spacer(Modifier.weight(0.7f))  // Remaining space to push content up
        }
    }
}

@Composable
fun UserDetailFields(
    pmdcNumber: String,
    pmdcNumberError: String?,
    institution: String,
    institutionError: String?,
    country: String,
    countryError: String?,
    city: String,
    cityError: String?,
    designation: String,
    designationError: String?,
    registrationNumber: String?,
    onEvent: (UserDetailsUiEvents) -> Unit
) {
    // Grouping all input fields into one section using AppTextField
    AppTextField(
        placeholder = stringResource(Res.string.enter_pmdc_number),
        text = pmdcNumber,
        errorMessage = pmdcNumberError,
        onTextChange = { onEvent(UserDetailsUiEvents.OnPmDcNumberTextChange(it)) }
    )
    AppTextField(
        placeholder = stringResource(Res.string.institution_name),
        text = institution,
        errorMessage = institutionError,
        onTextChange = { onEvent(UserDetailsUiEvents.OnInstitutionTextChange(it)) }
    )
    AppTextField(
        placeholder = stringResource(Res.string.country),
        text = country,
        errorMessage = countryError,
        onTextChange = { onEvent(UserDetailsUiEvents.OnCountryTextChange(it)) }
    )
    AppTextField(
        placeholder = stringResource(Res.string.city),
        text = city,
        errorMessage = cityError,
        onTextChange = { onEvent(UserDetailsUiEvents.OnCityTextChange(it)) }
    )
    AppTextField(
        placeholder = stringResource(Res.string.designation),
        text = designation,
        errorMessage = designationError,
        onTextChange = { onEvent(UserDetailsUiEvents.OnDesignationTextChange(it)) }
    )
    AppTextField(
        placeholder = stringResource(Res.string.enter_registration_number),
        text = registrationNumber ?: "",
        onTextChange = { onEvent(UserDetailsUiEvents.OnRegistrationNumberTextChange(it)) }
    )
}

