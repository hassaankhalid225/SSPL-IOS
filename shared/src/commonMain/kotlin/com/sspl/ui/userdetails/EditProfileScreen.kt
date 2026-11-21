package com.sspl.ui.userdetails

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.models.User
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.icons.SsplIcons
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PhoneWithCountrySelector
import com.sspl.ui.kyc.FormGroupLabel
import com.sspl.ui.kyc.GENDER_LABELS
import com.sspl.ui.kyc.GENDER_VALUES
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_ADDRESS
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_CITY
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_COUNTRY
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_FCPS_INSTITUTE
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_FCPS_YEAR
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_FIRST_NAME
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_GENDER
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_HOSPITAL
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_INSTITUTE
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_LAST_NAME
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_MBBS_INSTITUTE
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_MBBS_YEAR
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_MS_INSTITUTE
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_MS_YEAR
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_ORG_NUMBER
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_PHONE
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_PMDC
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_TITLE
import com.sspl.ui.userdetails.EditProfileViewModel.Companion.F_WHATSAPP
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

/**
 * Edit profile (inside the main shell) — one form with the same fields as the web "My profile"
 * page: personal, contact, professional and education (MBBS required). Save lives in a sticky
 * bottom bar; success / errors show as banners at the top like the web.
 */
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val prevHandle = navController.previousBackStackEntry?.savedStateHandle
    val userObj: User? = remember {
        prevHandle?.get<String>("currentUser")?.let {
            runCatching { Json { ignoreUnknownKeys = true }.decodeFromString(User.serializer(), it) }.getOrNull()
        }
    }
    LaunchedEffect(Unit) { viewModel.prefill(userObj) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Bring the result banner into view after a save attempt.
    LaunchedEffect(state.saveSuccess, state.formError) {
        if (state.saveSuccess || state.formError != null) scrollState.animateScrollTo(0)
    }

    val save: () -> Unit = {
        focusManager.clearFocus()
        viewModel.save()
    }
    val busy = state.isSaving
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    @Composable
    fun Field(
        key: String,
        label: String,
        placeholder: String? = null,
        icon: ImageVector? = null,
        keyboardType: KeyboardType = KeyboardType.Text,
        modifier: Modifier = Modifier,
        isLast: Boolean = false
    ) {
        SsplTextField(
            value = state.value(key),
            onValueChange = { viewModel.onFieldChange(key, it) },
            label = label,
            placeholder = placeholder,
            leadingIcon = icon,
            errorMessage = state.error(key),
            keyboardType = keyboardType,
            enabled = !busy,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
            keyboardActions = if (isLast) KeyboardActions(onDone = { save() }) else next,
            modifier = modifier
        )
    }

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EditIntro(
                    icon = SsplIcons.Person,
                    title = "My profile",
                    caption = "Keep your contact details and qualifications up to date for events, certificates and membership."
                )

                AnimatedVisibility(visible = state.saveSuccess) {
                    FeedbackBanner(message = "Profile updated successfully.")
                }
                AnimatedVisibility(visible = state.formError != null) {
                    FeedbackBanner(message = state.formError.orEmpty(), isError = true)
                }

                // Personal
                SsplCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FormGroupLabel("Personal")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Field(F_FIRST_NAME, "First name *", "First name", modifier = Modifier.weight(1f))
                            Field(F_LAST_NAME, "Last name *", "Last name", modifier = Modifier.weight(1f))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Gender", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
                            SegmentedTabs(
                                options = GENDER_LABELS,
                                selectedIndex = GENDER_VALUES.indexOf(state.gender),
                                onSelect = { if (!busy) viewModel.onGenderChange(GENDER_VALUES[it]) }
                            )
                            state.error(F_GENDER)?.let {
                                Text(it, style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.error))
                            }
                        }
                    }
                }

                // Contact
                SsplCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FormGroupLabel("Contact")
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Mobile number", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
                            PhoneWithCountrySelector(
                                selectedCountry = state.phoneCountry,
                                showTransformation = false,
                                phone = state.value(F_PHONE),
                                errorMessage = state.error(F_PHONE),
                                onPhoneNumberChange = { viewModel.onFieldChange(F_PHONE, it) },
                                showCountryChooser = state.showPhoneCountryChooser,
                                onCountryChange = viewModel::onPhoneCountryChange,
                                toggleCountryPicker = viewModel::togglePhoneCountryChooser
                            )
                            if (state.email.isNotBlank()) {
                                Text(
                                    "Signed in as ${state.email}. Your email address can't be changed here.",
                                    style = AppTheme.typography.bodySmall
                                )
                            }
                        }
                        Field(F_WHATSAPP, "WhatsApp", "+92 300 1234567", SsplIcons.Chat, KeyboardType.Phone)
                        Field(F_ADDRESS, "Address", "Street, area", SsplIcons.Home)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Field(F_CITY, "City", "Lahore", modifier = Modifier.weight(1f))
                            Field(F_COUNTRY, "Country", "Pakistan", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Professional
                SsplCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FormGroupLabel("Professional")
                        Field(F_PMDC, "PMDC number", "e.g. 12345-P", SsplIcons.MedicalServices)
                        Field(F_TITLE, "Designation", "e.g. Consultant Surgeon", SsplIcons.Work)
                        Field(F_HOSPITAL, "Hospital / clinic", "e.g. Services Hospital, Lahore", SsplIcons.Business)
                        Field(F_INSTITUTE, "Institute", "Organization name", SsplIcons.Business)
                        Field(F_ORG_NUMBER, "Society registration number", "Registration number", SsplIcons.Badge)
                    }
                }

                // Education (MBBS required, like the web)
                SsplCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FormGroupLabel("Education")
                        Field(F_MBBS_INSTITUTE, "MBBS institute *", "e.g. King Edward Medical University", SsplIcons.School)
                        Field(F_MBBS_YEAR, "MBBS passing year *", "e.g. 2015", keyboardType = KeyboardType.Number)
                        Field(F_FCPS_INSTITUTE, "FCPS institute", "Optional", SsplIcons.School)
                        Field(F_FCPS_YEAR, "FCPS passing year", "Optional", keyboardType = KeyboardType.Number)
                        Field(F_MS_INSTITUTE, "MS institute", "Optional", SsplIcons.School)
                        Field(F_MS_YEAR, "MS passing year", "Optional", keyboardType = KeyboardType.Number, isLast = true)
                    }
                }
                Text("* Required", style = AppTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
            }
            StickyCtaBar(
                primaryText = "Save profile",
                onPrimaryClick = save,
                loading = busy,
                enabled = !busy,
                secondaryText = "Back",
                onSecondaryClick = { navController.navigateUp() }
            )
        }
    }
}

@Composable
private fun EditIntro(icon: ImageVector, title: String, caption: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.titleMedium)
            Text(caption, style = AppTheme.typography.bodySmall)
        }
    }
}
